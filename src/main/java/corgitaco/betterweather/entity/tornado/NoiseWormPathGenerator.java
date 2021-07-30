package corgitaco.betterweather.entity.tornado;

import corgitaco.betterweather.util.noise.FastNoise;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Used to dynamically create a randomly generated path from 1 object to another.
 */
public class NoiseWormPathGenerator {
    private final List<Node> nodes;

    public NoiseWormPathGenerator(List<Node> nodes) {
        this.nodes = nodes;
    }

    public NoiseWormPathGenerator(FastNoise noise, BlockPos startPos, Predicate<BlockPos> isInvalid, int maxDistance) {
        List<Node> nodes = new ArrayList<>();

        nodes.add(new Node(startPos.toMutable(), 0));
        int distanceInNodes = maxDistance / 5;

        for (int i = 1; i < distanceInNodes; i++) {
            Node prevNode = nodes.get(i - 1);
            float angle = noise.GetNoise(prevNode.getPos().getX(), 0, prevNode.getPos().getZ());

            Vector2f dAngle = get2DAngle(angle * 5, 5);
            BlockPos previousNodePos = prevNode.getPos();
            Vector3i vecAngle = new Vector3i(dAngle.x, 0, dAngle.y);

            BlockPos addedPos = previousNodePos.add(vecAngle);
            int newY = 0;
            BlockPos.Mutable pos = new BlockPos.Mutable(addedPos.getX(), newY, addedPos.getZ());

            Node nextNode = new Node(pos, i);

            if (isInvalid.test(nextNode.getPos())) {
                break;
            }
            long key = ChunkPos.asLong(SectionPos.toChunk(nextNode.getPos().getX()), SectionPos.toChunk(nextNode.getPos().getZ()));

            nodes.add(nextNode);
        }

        this.nodes = nodes;
    }

    public boolean exists() {
        return nodes != null;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public BlockPos getFinalPosition() {
        return this.nodes.get(this.nodes.size() - 1).getPos();
    }

    public int getTotalNumberOfNodes() {
        return this.nodes.size();
    }

    public BlockPos getStartPos() {
        return this.nodes.get(0).getPos();
    }

    public static Vector2f get2DAngle(float angle, float length) {
        float x = (float) (Math.sin(angle) * length);
        float y = (float) (Math.cos(angle) * length);

        return new Vector2f(x, y);
    }

    public CompoundNBT write() {
        CompoundNBT nbt = new CompoundNBT();
        ListNBT positions = new ListNBT();
        for (Node node : this.nodes) {
            CompoundNBT posNBT = new CompoundNBT();
            posNBT.putInt("idx", node.getIdx());
            posNBT.putIntArray("pos", writeBlockPos(node.getPos().toImmutable()));
            positions.add(posNBT);
        }
        nbt.put("nodes", positions);
        return nbt;
    }

    public static NoiseWormPathGenerator read(CompoundNBT nbt) {
        List<Node> nodes = new ArrayList<>();

        ListNBT nodeNBTList = nbt.getList("nodes", 9);
        for (INBT inbt : nodeNBTList) {
           nodes.add(new Node(getBlockPos(((CompoundNBT) inbt).getIntArray("pos")).toMutable(), ((CompoundNBT) inbt).getInt("idx")));
        }

        return new NoiseWormPathGenerator(nodes);

    }

    public int[] writeBlockPos(BlockPos pos) {
        return new int[]{pos.getX(), pos.getY(), pos.getZ()};
    }

    public static BlockPos getBlockPos(int[] posArray) {
        return new BlockPos(posArray[0], posArray[1], posArray[2]);
    }


    public static class Node {
        private final int idx;
        private final BlockPos.Mutable pos;
        private float angleOffset;

        private Node(BlockPos.Mutable pos, int idx) {
            this.pos = pos;
            this.idx = idx;
        }

        public BlockPos.Mutable getPos() {
            return pos;
        }

        public int getIdx() {
            return idx;
        }

        public float getAngleOffset() {
            return angleOffset;
        }
    }
}