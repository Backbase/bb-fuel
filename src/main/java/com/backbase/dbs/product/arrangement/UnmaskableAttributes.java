package com.backbase.dbs.product.arrangement;

import com.backbase.dbs.arrangement.client.api.v2.model.MaskableAttribute;
import java.util.Set;

public interface UnmaskableAttributes {

    void setUnmaskableAttributes(Set<MaskableAttribute> attributes);

    Set<MaskableAttribute> getUnmaskableAttributes();

}
