package com.skyblockexp.ezrtp.platform;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public final class PaperChunkLoadStrategy implements ChunkLoadStrategy {

    private static final ChunkLoadInvoker INVOKER = resolveInvoker();

    @Override
    public CompletableFuture<Chunk> loadChunk(World world, int chunkX, int chunkZ) {
        return INVOKER.load(world, chunkX, chunkZ);
    }

    private static ChunkLoadInvoker resolveInvoker() {
        try {
            Method method = World.class.getMethod("getChunkAtAsync", int.class, int.class, boolean.class, boolean.class);
            return new FourArgChunkLoadInvoker(toFourArgInvoker(method));
        } catch (NoSuchMethodException ignored) {
            // Continue to older API signatures
        }

        try {
            Method method = World.class.getMethod("getChunkAtAsync", int.class, int.class, boolean.class);
            return new ThreeArgChunkLoadInvoker(toThreeArgInvoker(method));
        } catch (NoSuchMethodException ignored) {
            // Continue to oldest supported signature
        }

        try {
            Method method = World.class.getMethod("getChunkAtAsync", int.class, int.class);
            return new TwoArgChunkLoadInvoker(toTwoArgInvoker(method));
        } catch (NoSuchMethodException ignored) {
            return new SyncFallbackChunkLoadInvoker();
        }
    }

    private interface ChunkLoadInvoker {
        CompletableFuture<Chunk> load(World world, int chunkX, int chunkZ);
    }

    @FunctionalInterface
    private interface FourArgAsyncInvoker {
        CompletableFuture<Chunk> invoke(World world, int chunkX, int chunkZ, boolean generate, boolean urgent) throws Throwable;
    }

    @FunctionalInterface
    private interface ThreeArgAsyncInvoker {
        CompletableFuture<Chunk> invoke(World world, int chunkX, int chunkZ, boolean generate) throws Throwable;
    }

    @FunctionalInterface
    private interface TwoArgAsyncInvoker {
        CompletableFuture<Chunk> invoke(World world, int chunkX, int chunkZ) throws Throwable;
    }

    private static final class FourArgChunkLoadInvoker implements ChunkLoadInvoker {
        private final FourArgAsyncInvoker invoker;

        private FourArgChunkLoadInvoker(FourArgAsyncInvoker invoker) {
            this.invoker = invoker;
        }

        @Override
        public CompletableFuture<Chunk> load(World world, int chunkX, int chunkZ) {
            try {
                return invoker.invoke(world, chunkX, chunkZ, true, true);
            } catch (Throwable throwable) {
                return failedFuture(throwable);
            }
        }
    }

    private static final class ThreeArgChunkLoadInvoker implements ChunkLoadInvoker {
        private final ThreeArgAsyncInvoker invoker;

        private ThreeArgChunkLoadInvoker(ThreeArgAsyncInvoker invoker) {
            this.invoker = invoker;
        }

        @Override
        public CompletableFuture<Chunk> load(World world, int chunkX, int chunkZ) {
            try {
                return invoker.invoke(world, chunkX, chunkZ, true);
            } catch (Throwable throwable) {
                return failedFuture(throwable);
            }
        }
    }

    private static final class TwoArgChunkLoadInvoker implements ChunkLoadInvoker {
        private final TwoArgAsyncInvoker invoker;

        private TwoArgChunkLoadInvoker(TwoArgAsyncInvoker invoker) {
            this.invoker = invoker;
        }

        @Override
        public CompletableFuture<Chunk> load(World world, int chunkX, int chunkZ) {
            try {
                return invoker.invoke(world, chunkX, chunkZ);
            } catch (Throwable throwable) {
                return failedFuture(throwable);
            }
        }
    }

    private static final class SyncFallbackChunkLoadInvoker implements ChunkLoadInvoker {
        @Override
        public CompletableFuture<Chunk> load(World world, int chunkX, int chunkZ) {
            world.loadChunk(chunkX, chunkZ);
            return CompletableFuture.completedFuture(world.getChunkAt(chunkX, chunkZ));
        }
    }

    private static FourArgAsyncInvoker toFourArgInvoker(Method method) {
        MethodHandle handle = asHandle(method, MethodType.methodType(CompletableFuture.class, World.class, int.class, int.class, boolean.class, boolean.class));
        return (world, chunkX, chunkZ, generate, urgent) -> uncheckedCast((CompletableFuture<?>) handle.invokeExact(world, chunkX, chunkZ, generate, urgent));
    }

    private static ThreeArgAsyncInvoker toThreeArgInvoker(Method method) {
        MethodHandle handle = asHandle(method, MethodType.methodType(CompletableFuture.class, World.class, int.class, int.class, boolean.class));
        return (world, chunkX, chunkZ, generate) -> uncheckedCast((CompletableFuture<?>) handle.invokeExact(world, chunkX, chunkZ, generate));
    }

    private static TwoArgAsyncInvoker toTwoArgInvoker(Method method) {
        MethodHandle handle = asHandle(method, MethodType.methodType(CompletableFuture.class, World.class, int.class, int.class));
        return (world, chunkX, chunkZ) -> uncheckedCast((CompletableFuture<?>) handle.invokeExact(world, chunkX, chunkZ));
    }

    private static MethodHandle asHandle(Method method, MethodType methodType) {
        try {
            return MethodHandles.lookup().unreflect(method).asType(methodType);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access async chunk API method", e);
        }
    }

    private static CompletableFuture<Chunk> failedFuture(Throwable throwable) {
        CompletableFuture<Chunk> failed = new CompletableFuture<>();
        failed.completeExceptionally(throwable);
        return failed;
    }

    @SuppressWarnings("unchecked")
    private static CompletableFuture<Chunk> uncheckedCast(CompletableFuture<?> future) {
        return (CompletableFuture<Chunk>) future;
    }
}
