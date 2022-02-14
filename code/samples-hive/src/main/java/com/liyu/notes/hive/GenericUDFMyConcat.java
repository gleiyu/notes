package com.liyu.notes.hive;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorConverter;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.Text;


/**
 * hive udf 示例，实现concat函数拼接两个字符串
 */
@Description(name = "my_concat", value = "_FUNC_(str1,str2) - return the concatenation of str1 and str2",
        extended = "Example: _FUNC_('HELLO','HIVE');\n" +
                "HELLOHIVE")
public class GenericUDFMyConcat extends GenericUDF {
    private ObjectInspector[] argumentOIs;
    private PrimitiveObjectInspector.PrimitiveCategory returnType = PrimitiveObjectInspector.PrimitiveCategory.STRING;

    /**
     * 验证入参格式，定义函数出参
     *
     * @param arguments
     * @return
     * @throws UDFArgumentException
     */
    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        argumentOIs = arguments;
        if (arguments.length != 2) {
            throw new UDFArgumentLengthException("function my_concat needs 2 arguments.");
        }
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i].getCategory() != ObjectInspector.Category.PRIMITIVE) {
                throw new UDFArgumentException("my_concat only takes primitive arguments");
            }
        }
        return PrimitiveObjectInspectorFactory.writableStringObjectInspector;
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        PrimitiveObjectInspectorConverter.StringConverter[] stringConverters = new PrimitiveObjectInspectorConverter.StringConverter[arguments.length];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arguments.length; i++) {
            String val = null;
            stringConverters[i] = new PrimitiveObjectInspectorConverter.StringConverter((PrimitiveObjectInspector) argumentOIs[i]);
            if (arguments[i] != null) {
                val = (String) stringConverters[i].convert(arguments[i].get());
            }
            if (val == null) {
                return null;
            }
            sb.append(val);
        }
        return new Text(sb.toString());
    }

    @Override
    public String getDisplayString(String[] children) {
        return getStandardDisplayString("my_concat", children);
    }

}
