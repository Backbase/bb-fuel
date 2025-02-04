package com.backbase.ct.bbfuel.dto.accountStatement;

public enum ValidEstatementDocuments {

    MSWORD(new EstatetmentDocument("uid_001", DocumentType.MSWORD)),
    JPEG(new EstatetmentDocument("uid_002", DocumentType.JPEG)),
    PDF(new EstatetmentDocument("uid_003", DocumentType.PDF)),
    TEXT(new EstatetmentDocument("uid_004", DocumentType.TEXT)),
    PDF2(new EstatetmentDocument("YmFja2Jhc2U=", DocumentType.PDF));

    private final EstatetmentDocument eStatetmentDocument;

    private ValidEstatementDocuments(EstatetmentDocument eStatetmentDocument) {
        this.eStatetmentDocument = eStatetmentDocument;
    }

    public EstatetmentDocument geteStatetmentDocument() {
        return eStatetmentDocument;
    }
}
