package mxmustache.template;

public interface Function<T> {
	T apply() throws Exception;
}