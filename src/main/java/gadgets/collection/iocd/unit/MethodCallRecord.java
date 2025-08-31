package gadgets.collection.iocd.unit;

/**
 * 记录方法调用和调用类型信息的数据结构
 */
public class MethodCallRecord {
    public int hashCode; // 方法签名的哈希码
    public String methodSignature; // 方法签名
    public String callKind; // 调用类型：STATIC, VIRTUAL, INTERFACE, SPECIAL, UNKNOWN

    public MethodCallRecord() {
    }

    public MethodCallRecord(int hashCode, String methodSignature, String callKind) {
        this.hashCode = hashCode;
        this.methodSignature = methodSignature;
        this.callKind = callKind;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        MethodCallRecord that = (MethodCallRecord) obj;
        return hashCode == that.hashCode &&
                methodSignature.equals(that.methodSignature) &&
                callKind.equals(that.callKind);
    }

    @Override
    public int hashCode() {
        return methodSignature.hashCode() + callKind.hashCode();
    }

    @Override
    public String toString() {
        return methodSignature + " [" + callKind + "]";
    }
}
