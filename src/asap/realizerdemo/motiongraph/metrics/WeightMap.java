package asap.realizerdemo.motiongraph.metrics;

import asap.realizerdemo.motiongraph.Util;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Map for Joint-Weigths, where L_[joint] = R_[joint] = [joint].
 * <p>
 * @author yannick-broeker
 */
class WeightMap implements Map<String, Float> {

    /**
     * Returns a default WeightMap, where each contined joint has weight 1.
     * <p>
     * The contained Joints are HUMANOID_ROOT, VC7, VL3, VT9, SKULLBASE, ACROMIOCLAVICULAR, ANKLE, ELBOW, HIP, KNEE,
     * SHOULDER and WRIST.
     * <p>
     * @return default WeightMap
     */
    public static WeightMap getDefaultInstance() {
        WeightMap weightMap = new WeightMap(16);

        weightMap.weights.put(Util.HUMANOID_ROOT, 1f);
        weightMap.weights.put(Util.VC7, 1f);
        weightMap.weights.put(Util.VL3, 1f);
        weightMap.weights.put(Util.VT9, 1f);
        weightMap.weights.put(Util.SKULLBASE, 0.1f);
        weightMap.weights.put(Util.ACROMIOCLAVICULAR, 1f);
        weightMap.weights.put(Util.ANKLE, 4f);
        weightMap.weights.put(Util.ELBOW, 0.1f);
        weightMap.weights.put(Util.HIP, 10f);
        weightMap.weights.put(Util.KNEE, 6f);
        weightMap.weights.put(Util.SHOULDER, 1f);
        weightMap.weights.put(Util.WRIST, 0.1f);

        weightMap.weights.put(Util.VT10, 1f);
        weightMap.weights.put(Util.VT6, 1f);
        weightMap.weights.put(Util.VT1, 1f);
        weightMap.weights.put(Util.STERNOCLAVICULAR, 1f);

        return weightMap;
    }

    /**
     * Map for storing weights.
     */
    private final Map<String, Float> weights;

    /**
     * Creates a new Weightmap.
     */
    public WeightMap() {
        weights = new HashMap<>();
    }

    /**
     * Creates a new Weightmap with initialCapacity {@code initialCapacity}.
     */
    public WeightMap(int initialCapacity) {
        weights = new HashMap<>(initialCapacity);
    }

    @Override
    public int size() {
        return weights.size();
    }

    @Override
    public boolean isEmpty() {
        return weights.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        if (!(key instanceof String)) {
            return false;
        }
        if (((String) key).startsWith("L_") || ((String) key).startsWith("R_")) {
            return weights.containsKey(((String) key).substring(2));
        } else {
            return weights.containsKey(key);
        }
    }

    @Override
    public boolean containsValue(Object value) {
        return weights.containsValue(value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Float> m) {
        for (Entry<? extends String, ? extends Float> entrySet : m.entrySet()) {
            String key = entrySet.getKey();
            Float value = entrySet.getValue();
            this.put(key, value);
        }
    }

    @Override
    public void clear() {
        weights.clear();
    }

    @Override
    public Set<String> keySet() {
        return weights.keySet();
    }

    @Override
    public Collection<Float> values() {
        return weights.values();
    }

    @Override
    public Set<Entry<String, Float>> entrySet() {
        return weights.entrySet();
    }

    @Override
    public Float get(Object key) {
        if (!(key instanceof String) && (((String) key).startsWith("L_") || ((String) key).startsWith("R_"))) {
            return weights.get(((String) key).substring(2));

        } else {
            return weights.get(key);
        }
    }

    @Override
    public Float put(String key, Float value) {
        if (key.startsWith("L_") || key.startsWith("R_")) {
            return weights.put(key.substring(2), value);
        } else {
            return weights.put(key, value);
        }
    }

    @Override
    public Float remove(Object key) {
        if (!(key instanceof String) && (((String) key).startsWith("L_") || ((String) key).startsWith("R_"))) {
            return weights.remove(((String) key).substring(2));

        } else {
            return weights.remove(key);
        }
    }

}
