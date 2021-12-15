package io.xpipe.core.data.node;

import io.xpipe.core.data.DataStructureNode;
import io.xpipe.core.data.type.DataType;
import io.xpipe.core.data.type.TupleType;
import lombok.EqualsAndHashCode;

import java.util.*;

@EqualsAndHashCode(callSuper = false)
public class SimpleTupleNode extends TupleNode {

    private final List<String> names;
    private final List<DataStructureNode> nodes;

    SimpleTupleNode(List<String> names, List<DataStructureNode> nodes) {
        this.names = names;
        this.nodes = nodes;
    }

    @Override
    public DataType getDataType() {
        return TupleType.wrap(names, nodes.stream().map(DataStructureNode::getDataType).toList());
    }

    @Override
    protected String getName() {
        return "tuple node";
    }

    @Override
    public DataStructureNode at(int index) {
        return nodes.get(index);
    }

    @Override
    public DataStructureNode forKey(String name) {
        return nodes.get(names.indexOf(name));
    }

    @Override
    public Optional<DataStructureNode> forKeyIfPresent(String name) {
        if (!names.contains(name)) {
            return Optional.empty();
        }

        return Optional.of(nodes.get(names.indexOf(name)));
    }

    @Override
    public int size() {
        return nodes.size();
    }

    public String nameAt(int index) {
        return names.get(index);
    }

    @Override
    public List<KeyValue> getKeyValuePairs() {
        var l = new ArrayList<KeyValue>(size());
        for (int i = 0; i < size(); i++) {
            l.add(new KeyValue(getNames().get(i), getNodes().get(i)));
        }
        return l;
    }

    public List<String> getNames() {
        return Collections.unmodifiableList(names);
    }

    public List<DataStructureNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }
}