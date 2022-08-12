package io.xpipe.core.store;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.xpipe.core.util.Secret;
import lombok.Value;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@JsonTypeName("local")
@Value
public class LocalStore extends StandardShellStore implements MachineFileStore {

    @Override
    public boolean isLocal() {
        return true;
    }

    static class LocalProcessControl extends ProcessControl {

        private final List<Secret> input;
        private final ProcessBuilder builder;
        private final Integer timeout;

        private Process process;

        LocalProcessControl(List<Secret> input, List<String> cmd, Integer timeout) {
            this.input = input;
            this.timeout = timeout;
            var l = new ArrayList<String>();
            l.add("cmd");
            l.add("/c");
            l.addAll(cmd);
            builder = new ProcessBuilder(l);
        }

        private InputStream createInputStream() {
            var string = input.stream().map(secret -> secret.getSecretValue()).collect(Collectors.joining("\n")) + "\r\n";
            return new ByteArrayInputStream(string.getBytes(StandardCharsets.US_ASCII));
        }

        @Override
        public void start() throws IOException {
            process = builder.start();

            var t = new Thread(() -> {
                try (var inputStream = createInputStream()){
                    process.getOutputStream().flush();
                    inputStream.transferTo(process.getOutputStream());
                    process.getOutputStream().close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            t.setDaemon(true);
            t.start();
        }

        @Override
        public int waitFor() throws Exception {
            if (timeout != null) {
                return process.waitFor(timeout, TimeUnit.SECONDS) ? 0 : -1;
            } else {
                return process.waitFor();
            }
        }

        @Override
        public InputStream getStdout() {
            return process.getInputStream();
        }

        @Override
        public InputStream getStderr() {
            return process.getErrorStream();
        }

        @Override
        public Charset getCharset() {
            return StandardCharsets.US_ASCII;
        }

        public Integer getTimeout() {
            return timeout;
        }
    }

    @Override
    public boolean exists(String file) {
        return Files.exists(Path.of(file));
    }

    @Override
    public String toSummaryString() {
        return "localhost";
    }

    @Override
    public InputStream openInput(String file) throws Exception {
        var p = Path.of(file);
        return Files.newInputStream(p);
    }

    @Override
    public OutputStream openOutput(String file) throws Exception {
        var p = Path.of(file);
        return Files.newOutputStream(p);
    }

    @Override
    public ProcessControl prepareCommand(List<Secret> input, List<String> cmd, Integer timeout) {
        return new LocalProcessControl(input, cmd, getEffectiveTimeOut(timeout));
    }

    @Override
    public ProcessControl preparePrivilegedCommand(List<Secret> input, List<String> cmd, Integer timeOut) throws Exception {
        return new LocalProcessControl(input, cmd, getEffectiveTimeOut(timeOut));
    }

    @Override
    public ShellType determineType() throws Exception {
        return ShellTypes.determine(this);
    }
}
