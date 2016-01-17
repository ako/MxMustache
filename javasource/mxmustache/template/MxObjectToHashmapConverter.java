package mxmustache.template;

import com.mendix.core.Core;
import com.mendix.core.objectmanagement.member.MendixEnum;
import com.mendix.core.objectmanagement.member.MendixObjectReference;
import com.mendix.core.objectmanagement.member.MendixObjectReferenceSet;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixIdentifier;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.IMendixObjectMember;
import com.mendix.systemwideinterfaces.core.meta.IMetaAssociation;
import com.mendix.systemwideinterfaces.core.meta.IMetaObject;
import mxmustache.proxies.BooleanValue;
import mxmustache.proxies.Primitive;

import java.util.*;

/**
 * Created by ako on 1/3/2016.
 */
public class MxObjectToHashmapConverter {
    private static final ILogNode LOGGER = Core.getLogger(MxObjectToHashmapConverter.class.getName());

    /**
     * returns a json string containingURL if id is persistable or json object if with the json representation if the object is not. s
     *
     * @param context
     * @param id
     * @param useServiceUrls
     * @return
     * @throws Exception
     */
    public static Object identifierToHashMap(IContext context, IMendixIdentifier id, boolean useServiceUrls, int maxDepth) throws Exception {
        return identifierToHashMap(context, id, new HashSet<Long>(), useServiceUrls, maxDepth);
    }


    private static Object identifierToHashMap(IContext context, IMendixIdentifier id, Set<Long> alreadySeen, boolean useServiceUrls, int maxDepth) throws Exception {
        if (id == null)
            return null;

        if (maxDepth < 0) {
            LOGGER.warn("Max depth");
            return null;
        }

        if (alreadySeen.contains(id.toLong())) {
            LOGGER.warn("ID already seen: " + id.toLong() + ", skipping serialization");
            return null;
        }
        alreadySeen.add(id.toLong());

        IMendixObject obj = Core.retrieveId(context, id); //Optimize: for refset use retrieve ids
        if (obj == null) {
            LOGGER.warn("Failed to retrieve identifier: " + id + ", does the object still exist?");
            return null;
        } else if (obj.getType().equals(Primitive.entityName)) {
            return writePrimitiveToJson(context, Primitive.initialize(context, obj));
        } else
            return writeMxObjectToHashMap(context, obj, alreadySeen, useServiceUrls, maxDepth - 1);
    }

    private static Object writePrimitiveToJson(IContext context, Primitive primitive) {
        if (primitive.getPrimitiveType() == null)
            throw new IllegalStateException("PrimitiveType attribute of RestServices.Primitive should be set");

        switch (primitive.getPrimitiveType()) {
            case Number:
                return primitive.getNumberValue();
            case String:
                return primitive.getStringValue();
            case _NULL:
                return null;
            case _Boolean:
                return primitive.getBooleanValue();
            default:
                throw new IllegalStateException("PrimitiveType attribute of RestServices.Primitive should be set");
        }
    }


    public static HashMap writeMxObjectToHashMap(IContext context, IMendixObject view, int maxDepth) throws Exception {
        return writeMxObjectToHashMap(context, view, false, maxDepth);
    }

    public static HashMap writeMxObjectToHashMap(IContext context, IMendixObject view, boolean useServiceUrls, int maxDepth) throws Exception {
        return writeMxObjectToHashMap(context, view, new HashSet<Long>(), useServiceUrls, maxDepth);
    }

    private static HashMap writeMxObjectToHashMap(IContext context, IMendixObject view, Set<Long> alreadySeen, boolean useServiceUrls, int maxDepth) throws Exception {
        if (view == null)
            throw new IllegalArgumentException("Mendix to HashMap conversion expects an object");

        if (!Utils.hasDataAccess(view.getMetaObject(), context))
            throw new IllegalStateException("During HashMap serialization: Object of type '" + view.getType() + "' has no readable members for users with role(s) " + context.getSession().getUserRolesNames() + ". Please check the security rules");

        HashMap res = new HashMap();
        alreadySeen.add(view.getId().toLong());

        Map<String, ? extends IMendixObjectMember<?>> members = view.getMembers(context);
        for (java.util.Map.Entry<String, ? extends IMendixObjectMember<?>> e : members.entrySet())
            serializeMember(context, res, getTargetMemberName(context, view, e.getKey()), e.getValue(), view.getMetaObject(), alreadySeen, useServiceUrls, maxDepth);

        Collection dmac = view.getMetaObject().getDeclaredMetaAssociationsChild();
        Iterator d = dmac.iterator();
        while (d.hasNext()) {
            IMetaAssociation ma = (IMetaAssociation) d.next();

            List<IMendixObject> list = Core.retrieveByPath(context, view, ma.getName());
            Iterator<IMendixObject> it = list.iterator();
            ArrayList ar = new ArrayList();
            while (it.hasNext()) {
                IMendixObject obj2 = it.next();
                Object value = obj2.getId();
                if (value != null) {
                    value = identifierToHashMap(context, (IMendixIdentifier) value, alreadySeen, useServiceUrls, maxDepth);
                    ar.add(value);
                }
            }
            res.put(ma.getName().split("\\.")[1], ar);
        }

        return res;
    }

    private static String getTargetMemberName(IContext context,
                                              IMendixObject view, String sourceAttr) {
        String name = Utils.getShortMemberName(sourceAttr);
        if (name == null || name.trim().isEmpty())
            throw new IllegalStateException("During HashMap serialization: Object of type '" + view.getType() + "', member '" + sourceAttr + "' has a corresponding '_jsonkey' attribute, but its value is empty.");

        return name;
    }


    @SuppressWarnings("deprecation")
    private static void serializeMember(IContext context, HashMap target, String targetMemberName,
                                        IMendixObjectMember<?> member, IMetaObject viewType, Set<Long> alreadySeen,
                                        boolean useServiceUrls, int maxDepth) throws Exception {
        if (context == null)
            throw new IllegalStateException("Context is null");

        Object value = member.getValue(context);
        String memberName = member.getName();

        if (Utils.isSystemAttribute(memberName)) {
            //skip
        }
        //Primitive?
        else if (!(member instanceof MendixObjectReference) && !(member instanceof MendixObjectReferenceSet)) {

            switch (viewType.getMetaPrimitive(member.getName()).getType()) {
                case AutoNumber:
                case Long:
                case Boolean:
                case Currency:
                case Float:
                case Integer:
                    if (value == null) {
                        LOGGER.warn("Got 'null' as value for primitive '" + targetMemberName + "'");
                        target.put(targetMemberName, null);
                    } else {
                        target.put(targetMemberName, value);
                    }
                    break;
                case Enum:
                    //Support for built-in BooleanValue enumeration.
                    MendixEnum me = (MendixEnum) member;
                    if ("RestServices.BooleanValue".equals(me.getEnumeration().getName())) {
                        if (BooleanValue._true.toString().equals(me.getValue(context)))
                            target.put(targetMemberName, true);
                        else if (BooleanValue._false.toString().equals(me.getValue(context)))
                            target.put(targetMemberName, false);
                        break;
                    }

                    //other enumeration, fall trough intentional
                case HashString:
                case String:
                    if (value == null)
                        target.put(targetMemberName, null);
                    else
                        target.put(targetMemberName, value);
                    break;
                case Decimal:
                    if (value == null)
                        target.put(targetMemberName, null);
                    else
                        target.put(targetMemberName, value);
                    break;
                case DateTime:
                    if (value == null)
                        target.put(targetMemberName, null);
                    else
                        target.put(targetMemberName, value);
                    break;
                case Binary:
                    break;
                default:
                    throw new IllegalStateException("Not supported Mendix Membertype for member " + memberName);
            }
        }

        /**
         * Reference
         */
        else if (member instanceof MendixObjectReference) {
            if (value != null)
                value = identifierToHashMap(context, (IMendixIdentifier) value, alreadySeen, useServiceUrls, maxDepth);

            if (value == null)
                target.put(targetMemberName, null);
            else
                target.put(targetMemberName, value);
        }

        /**
         * Referenceset
         */
        else if (member instanceof MendixObjectReferenceSet) {
            ArrayList ar = new ArrayList();
            if (value != null) {
                @SuppressWarnings("unchecked")
                List<IMendixIdentifier> ids = (List<IMendixIdentifier>) value;
                Utils.sortIdList(ids);
                for (IMendixIdentifier id : ids)
                    if (id != null) {
                        Object url = identifierToHashMap(context, id, alreadySeen, useServiceUrls, maxDepth);
                        if (url != null)
                            ar.add(url);
                    }
            }
            target.put(targetMemberName, ar);
        } else
            throw new IllegalStateException("Unimplemented membertype " + member.getClass().getSimpleName());
    }
}
