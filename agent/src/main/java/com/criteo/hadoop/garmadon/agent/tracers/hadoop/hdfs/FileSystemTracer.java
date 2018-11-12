package com.criteo.hadoop.garmadon.agent.tracers.hadoop.hdfs;

import com.criteo.hadoop.garmadon.agent.tracers.MethodTracer;
import com.criteo.hadoop.garmadon.event.proto.DataAccessEventProtos;
import com.criteo.hadoop.garmadon.schema.enums.FsAction;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.util.Progressable;

import java.lang.instrument.Instrumentation;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.bytebuddy.implementation.MethodDelegation.to;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class FileSystemTracer {

    private static BiConsumer<Long, Object> eventHandler;

    public static void setup(Instrumentation instrumentation, BiConsumer<Long, Object> eventConsumer) {

        initEventHandler(eventConsumer);

        new ReadTracer().installOn(instrumentation);
        new WriteTracer().installOn(instrumentation);
        new RenameTracer().installOn(instrumentation);
        new DeleteTracer().installOn(instrumentation);
        new AppendTracer().installOn(instrumentation);
    }

    public static void initEventHandler(BiConsumer<Long, Object> eventConsumer) {
        FileSystemTracer.eventHandler = eventConsumer;
    }

    public static class DeleteTracer extends MethodTracer {

        @Override
        public ElementMatcher<? super TypeDescription> typeMatcher() {
            return nameStartsWith("org.apache.hadoop.hdfs.DistributedFileSystem");
        }

        @Override
        public ElementMatcher<? super MethodDescription> methodMatcher() {
            return named("delete").and(takesArguments(Path.class, boolean.class));
        }

        @Override
        public Implementation newImplementation() {
            return to(DeleteTracer.class).andThen(SuperMethodCall.INSTANCE);
        }

        public static void intercept(
                @This Object o,
                @Argument(0) Path dst) throws Exception {
            Object uri = ((DistributedFileSystem) o).getUri();
            DataAccessEventProtos.FsEvent event = DataAccessEventProtos.FsEvent
                    .newBuilder()
                    .setAction(FsAction.DELETE.name())
                    .setDstPath(dst.toString())
                    .setUri(uri.toString())
                    .build();
            eventHandler.accept(System.currentTimeMillis(), event);
        }

    }

    public static class ReadTracer extends MethodTracer {

        @Override
        public ElementMatcher<? super TypeDescription> typeMatcher() {
            return nameStartsWith("org.apache.hadoop.hdfs.DistributedFileSystem");
        }

        @Override
        public ElementMatcher<? super MethodDescription> methodMatcher() {
            return named("open").and(takesArguments(Path.class, int.class));
        }

        @Override
        public Implementation newImplementation() {
            return to(ReadTracer.class).andThen(SuperMethodCall.INSTANCE);
        }

        public static void intercept(
                @This Object o,
                @Argument(0) Path dst) throws Exception {
            Object uri = ((DistributedFileSystem) o).getUri();
            DataAccessEventProtos.FsEvent event = DataAccessEventProtos.FsEvent
                    .newBuilder()
                    .setAction(FsAction.READ.name())
                    .setDstPath(dst.toString())
                    .setUri(uri.toString())
                    .build();
            eventHandler.accept(System.currentTimeMillis(), event);
        }
    }

    public static class RenameTracer extends MethodTracer {

        @Override
        public ElementMatcher<? super TypeDescription> typeMatcher() {
            return nameStartsWith("org.apache.hadoop.hdfs.DistributedFileSystem");
        }

        @Override
        public ElementMatcher<? super MethodDescription> methodMatcher() {
            return named("rename");
        }

        @Override
        public Implementation newImplementation() {
            return to(RenameTracer.class).andThen(SuperMethodCall.INSTANCE);
        }

        public static void intercept(
                @This Object o,
                @Argument(0) Path src,
                @Argument(1) Path dst) throws Exception {
            Object uri = ((DistributedFileSystem) o).getUri();
            DataAccessEventProtos.FsEvent event = DataAccessEventProtos.FsEvent
                    .newBuilder()
                    .setAction(FsAction.RENAME.name())
                    .setSrcPath(src.toString())
                    .setDstPath(dst.toString())
                    .setUri(uri.toString())
                    .build();
            eventHandler.accept(System.currentTimeMillis(), event);
        }
    }

    public static class WriteTracer extends MethodTracer {

        @Override
        public ElementMatcher<? super TypeDescription> typeMatcher() {
            return nameStartsWith("org.apache.hadoop.hdfs.DistributedFileSystem");
        }

        @Override
        public ElementMatcher<? super MethodDescription> methodMatcher() {
            return named("create").and(
                    takesArguments(
                            Path.class,
                            FsPermission.class,
                            boolean.class,
                            int.class,
                            short.class,
                            long.class,
                            Progressable.class
                    ));
        }

        @Override
        public Implementation newImplementation() {
            return to(WriteTracer.class).andThen(SuperMethodCall.INSTANCE);
        }

        public static void intercept(@This Object o, @Argument(0) Path dst) throws Exception {
            Object uri = ((DistributedFileSystem) o).getUri();
            DataAccessEventProtos.FsEvent event = DataAccessEventProtos.FsEvent
                    .newBuilder()
                    .setAction(FsAction.WRITE.name())
                    .setDstPath(dst.toString())
                    .setUri(uri.toString())
                    .build();
            eventHandler.accept(System.currentTimeMillis(), event);
        }
    }

    public static class AppendTracer extends MethodTracer {

        @Override
        public ElementMatcher<? super TypeDescription> typeMatcher() {
            return nameStartsWith("org.apache.hadoop.hdfs.DistributedFileSystem");
        }

        @Override
        public ElementMatcher<? super MethodDescription> methodMatcher() {
            return named("append").and(
                    takesArguments(
                            Path.class,
                            int.class,
                            Progressable.class
                    )
            );
        }

        @Override
        public Implementation newImplementation() {
            return to(AppendTracer.class).andThen(SuperMethodCall.INSTANCE);
        }

        public static void intercept(@This Object o, @Argument(0) Path dst) throws Exception {
            Object uri = ((DistributedFileSystem) o).getUri();
            DataAccessEventProtos.FsEvent event = DataAccessEventProtos.FsEvent
                    .newBuilder()
                    .setAction(FsAction.APPEND.name())
                    .setDstPath(dst.toString())
                    .setUri(uri.toString())
                    .build();
            eventHandler.accept(System.currentTimeMillis(), event);
        }
    }
}
