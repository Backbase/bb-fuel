package com.backbase.ct.bbfuel.dto.accountStatement;

import com.backbase.ct.bbfuel.dto.accountStatement.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
@AllArgsConstructor
public class EstatetmentDocument {

    private String uid;
    private String url;
    private DocumentType contentType;

    public EstatetmentDocument() {
    }

    public EstatetmentDocument(String uid, DocumentType contentType) {
        this.uid = uid;
        this.contentType = contentType;
    }
}
