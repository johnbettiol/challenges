package com.jbettiol.ewddemo.dropbox;

@FunctionalInterface
interface DropboxActionResolver<T> {

    T perform() throws Exception;

}
