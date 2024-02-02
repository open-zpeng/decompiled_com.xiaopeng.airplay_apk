package org.fourthline.cling.binding.staging;

import java.util.List;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.meta.StateVariableAllowedValueRange;
import org.fourthline.cling.model.meta.StateVariableEventDetails;
import org.fourthline.cling.model.meta.StateVariableTypeDetails;
import org.fourthline.cling.model.types.Datatype;
/* loaded from: classes.dex */
public class MutableStateVariable {
    public MutableAllowedValueRange allowedValueRange;
    public List<String> allowedValues;
    public Datatype dataType;
    public String defaultValue;
    public StateVariableEventDetails eventDetails;
    public String name;

    public StateVariable build() {
        String[] strArr;
        String str = this.name;
        Datatype datatype = this.dataType;
        String str2 = this.defaultValue;
        if (this.allowedValues != null && this.allowedValues.size() != 0) {
            strArr = (String[]) this.allowedValues.toArray(new String[this.allowedValues.size()]);
        } else {
            strArr = null;
        }
        return new StateVariable(str, new StateVariableTypeDetails(datatype, str2, strArr, this.allowedValueRange != null ? new StateVariableAllowedValueRange(this.allowedValueRange.minimum.longValue(), this.allowedValueRange.maximum.longValue(), this.allowedValueRange.step.longValue()) : null), this.eventDetails);
    }
}
