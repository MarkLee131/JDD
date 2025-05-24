package gadgets.unit;

import cfg.Node;
import container.BasicDataContainer;
import container.FragmentsContainer;
import dataflow.node.MethodDescriptor;
import gadgets.collection.markers.Comparison;
import gadgets.collection.node.ConditionNode;
import gadgets.collection.node.ConditionUtils;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;
import soot.jimple.*;
import tranModel.Rules.RuleUtils;
import tranModel.TransformableNode;
import util.Pair;
import util.StaticAnalyzeUtils.Parameter;
import util.Utils;

import java.util.HashMap;
import java.util.HashSet;

import static dataflow.DataFlow.findAllDefUnitAffectThisValue;
import static tranModel.Rules.RuleUtils.getValueByParamIndex;

public class ConnectRequire {
    public HashSet<SootMethod> preLinkableMethods = new HashSet<>(); // Fragment 跳转条件
    public HashSet<HashSet<Integer>> paramsTaitRequire = null; // 污点要求
    /** 其他链接条件 */
    // 目前考虑两种: 方法名限制 / 方法所属的类型限制
    public HashMap<String, HashSet<String>> dynamicProxyLinkCheck = new HashMap<>();
    

    // 记录反射拼接的fragment的拼接要求
    // static(1) getter(1)/Interface(2)/any(0) non-parameter(0默认)/String(1)
    public String reflectionCheck = "010";

    public ConnectRequire(HashSet<HashSet<Integer>> paramsTaitRequire, HashSet<SootMethod> preLinkableMethods){
        dynamicProxyLinkCheck.put("MethodNameBlackList", new HashSet<>());
        dynamicProxyLinkCheck.put("MethodNameWhiteList", new HashSet<>());
        dynamicProxyLinkCheck.put("DecClassBlackList", new HashSet<>());
        dynamicProxyLinkCheck.put("DecClassWhiteList", new HashSet<>());
        this.paramsTaitRequire = paramsTaitRequire;
        this.preLinkableMethods = preLinkableMethods;
    }

    public ConnectRequire(HashSet<SootMethod> preLinkableMethods){
        dynamicProxyLinkCheck.put("MethodNameBlackList", new HashSet<>());
        dynamicProxyLinkCheck.put("MethodNameWhiteList", new HashSet<>());
        dynamicProxyLinkCheck.put("DecClassBlackList", new HashSet<>());
        dynamicProxyLinkCheck.put("DecClassWhiteList", new HashSet<>());
        this.preLinkableMethods = preLinkableMethods;
    }

    public boolean satisfyDynamicProxyFragmentLinkCondition(String methodName, String className, Fragment preFragment){
        if (preFragment.directSource == null
                || preFragment.directSource.getClassOfType() == null
                || !(preFragment.directSource.getClassOfType().isInterface() || RuleUtils.isGeneticType(preFragment.directSource.getType())))
            return false;
//        return true;
//        if (dynamicProxyLinkCheck.get("MethodNameBlackList").contains(methodName))
//            return false;
//        if (dynamicProxyLinkCheck.get("DecClassBlackList").contains(className))
//            return false;
//        if (!dynamicProxyLinkCheck.get("MethodNameWhiteList").isEmpty() && !dynamicProxyLinkCheck.get("MethodNameWhiteList").contains(methodName))
//            return false;
//        if (!dynamicProxyLinkCheck.get("DecClassWhiteList").isEmpty() && !dynamicProxyLinkCheck.get("DecClassWhiteList").contains(methodName))
//            return false;
        return true;
    }

    public void parseAndAddDynamicProxyLinkCondition(TransformableNode tfNode, boolean satisfyFlag){
        MethodDescriptor tmpDescriptor = BasicDataContainer.getOrCreateDescriptor(tfNode.method);
        Value conditionValue = ((IfStmt) tfNode.node.unit).getCondition();
        Comparison comparison = RuleUtils.parseComparison(conditionValue, satisfyFlag);

        boolean reverse = true;

        for (ValueBox valueBox: conditionValue.getUseBoxes()){
            // 如果是常量，则认为是条件限制变量
            if (valueBox.getValue() instanceof Constant){ // Constant
                if (reverse & !comparison.equals(Comparison.EQUAL) & !comparison.equals(Comparison.NO_EQUAL_TO))
                    RuleUtils.flipComparison(comparison);

            }else {
                // 可能存在一些复杂一些的数据流，查找所有定义语句并进行进一步解析
                HashSet<Node> sources = findAllDefUnitAffectThisValue(tfNode.node, valueBox);
                for (Node source: sources){
                    if (source.unit instanceof AssignStmt){
                        AssignStmt assignStmt = (AssignStmt) source.unit;
                        Value left = assignStmt.getLeftOp();
                        Value right = assignStmt.getRightOp();
                        if (right instanceof InvokeExpr){
                            SootMethod invokedMethod = ((InvokeExpr)right).getMethod();
                            if (ConditionUtils.compareMethodsMapInputArg.containsKey(invokedMethod.getSignature())){
                                Pair<Integer, Integer> inds = ConditionUtils.compareMethodsMapInputArg.get(invokedMethod.getSignature());
                                // 更新一下比较符号
                                if (invokedMethod.getName().contains("equal")){
                                    if (comparison.equals(Comparison.EQUAL))
                                        comparison = Comparison.NO_EQUAL_TO;
                                    if (comparison.equals(Comparison.NO_EQUAL_TO))
                                        comparison = Comparison.EQUAL;
                                }

                                Value compareValue = getValueByParamIndex((Stmt) source.unit, inds.getKey());
                                Value comparedValue = getValueByParamIndex((Stmt) source.unit, inds.getValue());
                                if (comparedValue != null & compareValue != null){
                                    if (comparedValue instanceof Constant){

                                    }
                                    else
                                        tmpDescriptor.sourcesTaintGraph.matchTaintedSources(comparedValue);
                                    if (compareValue instanceof Constant){
                                        System.out.println(compareValue);
                                    }
                                    else
                                        tmpDescriptor.sourcesTaintGraph.matchTaintedSources(compareValue);
                                }
                            }
//                            else if (invokedMethod.getName().startsWith("getMethod")){
//                                HashSet<String> methodNames = new HashSet<>();
//                                for (Value argValue: ((InvokeExpr) right).getArgs()){
//                                    if (argValue instanceof Constant){
//                                        methodNames.add(argValue.toString().replaceAll("\"", ""));
//                                    }
//                                }
//                                if (comparison.equals(Comparison.EQUAL))
//                                    this.dynamicProxyLinkCheck.get("MethodNameWhiteList").addAll(methodNames);
//                                else this.dynamicProxyLinkCheck.get("MethodNameBlackList").addAll(methodNames);
//                            }
                        }
                    }
                }
            }
            reverse = false;
        }
    }

}
