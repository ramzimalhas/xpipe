package io.xpipe.extension;

import io.xpipe.core.charsetter.NewLine;
import io.xpipe.core.charsetter.StreamCharset;
import io.xpipe.core.dialog.Dialog;
import io.xpipe.core.dialog.QueryConverter;
import io.xpipe.core.store.DataStore;
import io.xpipe.core.store.LocalStore;
import io.xpipe.core.store.MachineFileStore;
import io.xpipe.core.store.ShellStore;
import io.xpipe.core.util.Secret;
import lombok.Value;

public class DialogHelper {

    @Value
    public static class Address {
        String hostname;
        Integer port;
    }

    public static Dialog addressQuery(Address address) {
        var hostNameQuery = Dialog.query("Hostname", false, true, false, address.getHostname(), QueryConverter.STRING);
        var portQuery = Dialog.query("Port", false, true, false, address.getPort(), QueryConverter.INTEGER);
        return Dialog.chain(hostNameQuery, portQuery)
                .evaluateTo(() -> new Address(hostNameQuery.getResult(), portQuery.getResult()));
    }

    public static Dialog machineQuery(DataStore store) {
        var storeName = XPipeDaemon.getInstance()
                .getStoreName(store)
                .orElse("local");
        return Dialog.query("Machine", false, true, false, storeName, QueryConverter.STRING)
                .map((String name) -> {
                    if (name.equals("local")) {
                        return new LocalStore();
                    }

                    var stored = XPipeDaemon.getInstance()
                            .getNamedStore(name);
                    if (stored.isEmpty()) {
                        throw new IllegalArgumentException(String.format("Store not found: %s", name));
                    }

                    if (!(stored.get() instanceof MachineFileStore)) {
                        throw new IllegalArgumentException(String.format("Store not a machine store: %s", name));
                    }

                    return stored.get();
                });
    }

    public static Dialog shellQuery(DataStore store) {
        var storeName = XPipeDaemon.getInstance()
                .getStoreName(store)
                .orElse("local");
        return Dialog.query("Shell", false, true, false, storeName, QueryConverter.STRING)
                .map((String name) -> {
                    if (name.equals("local")) {
                        return new LocalStore();
                    }

                    var stored = XPipeDaemon.getInstance()
                            .getNamedStore(name);
                    if (stored.isEmpty()) {
                        throw new IllegalArgumentException(String.format("Store not found: %s", name));
                    }

                    if (!(stored.get() instanceof ShellStore)) {
                        throw new IllegalArgumentException(String.format("Store not a shell store: %s", name));
                    }

                    return stored.get();
                });
    }

    public static Dialog charsetQuery(StreamCharset c,  boolean all) {
        return Dialog.query("Charset", false, true, c != null &&!all, c, QueryConverter.CHARSET);
    }

    public static Dialog newLineQuery(NewLine n, boolean all) {
        return Dialog.query("Newline", false, true, n != null &&!all, n, QueryConverter.NEW_LINE);
    }


    public static <T> Dialog query(String desc, T value, boolean required, QueryConverter<T> c, boolean all)  {
        return Dialog.query(desc, false, required, value != null && !all, value, c);
    }

    public static Dialog fileQuery(String name) {
        return Dialog.query("File", true, true, false, name, QueryConverter.STRING);
    }

    public static Dialog userQuery(String name) {
        return Dialog.query("User", false, true, false, name, QueryConverter.STRING);
    }

    public static Dialog namedStoreQuery(DataStore store, Class<? extends DataStore> filter) {
        var name = XPipeDaemon.getInstance()
                .getStoreName(store)
                .orElse(null);
        return Dialog.query("Store", false, true, false, name, QueryConverter.STRING)
                .map((String newName) -> {
                    var found =  XPipeDaemon.getInstance()
                            .getNamedStore(newName)
                            .orElseThrow();
                    if (!filter.isAssignableFrom(found.getClass())) {
                        throw new IllegalArgumentException("Incompatible store type");
                    }
                    return found;
                });
    }

    public static Dialog passwordQuery(Secret password) {
        return Dialog.querySecret("Password", false, true, password);
    }

    public static Dialog timeoutQuery(Integer timeout) {
        return Dialog.query("Timeout", false, true, false, timeout, QueryConverter.INTEGER);
    }

}