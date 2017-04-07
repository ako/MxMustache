rm userlib/*
java -jar build\apache-ivy-2.4.0\ivy-2.4.0.jar -ivy ivy.xml -confs export-mustache -retrieve "userlib/[artifact]-[revision].[ext]"
for %%f in (userlib\*) do touch %%f.MxUtils-Mustache.RequiredLib