package gadgets.unit;

import dataflow.node.MethodDescriptor;
import soot.SootMethod;
import tranModel.Transformable;
import tranModel.TransformableNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;

public class PathRecord {
    public HashMap<LinkedList<SootMethod>, LinkedHashSet<TransformableNode>> callStack2Transformable = new HashMap<>(); // 不记录之前其他执行分支的信息
    /**
     * 记录动态代理常见的路径条件约束信息
     * @param tfNode
     */
    public void recordPathCondition(LinkedList<SootMethod> callStack, SootMethod invokedMethod, TransformableNode tfNode){
        LinkedList<SootMethod> tmpCallStack = new LinkedList<>(callStack);
        tmpCallStack.add(invokedMethod);
        if (!callStack2Transformable.containsKey(tmpCallStack))
            callStack2Transformable.put(tmpCallStack, new LinkedHashSet<>());
        if (callStack2Transformable.containsKey(callStack))
            callStack2Transformable.get(tmpCallStack).addAll(callStack2Transformable.get(callStack));
        callStack2Transformable.get(tmpCallStack).add(tfNode);
    }

    public LinkedHashSet<TransformableNode> getInvokeTransforms(LinkedList<SootMethod> gadgets) {
        return callStack2Transformable.get(gadgets);
    }

    public LinkedHashSet<TransformableNode> getInvokeTransforms(LinkedList<SootMethod> gadgets, SootMethod invokedMethod) {
        LinkedList<SootMethod> tmpCallStack = new LinkedList<>(gadgets);
        tmpCallStack.remove(invokedMethod);
        return callStack2Transformable.get(tmpCallStack);
    }

    public void deletePathCondition(LinkedList<SootMethod> callStack) {
        callStack2Transformable.remove(callStack);
    }
}
