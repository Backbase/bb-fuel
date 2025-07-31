package com.backbase.ct.bbfuel.dto.accountStatement;

public enum ValidEstatementDocuments {

    MSWORD(new EstatetmentDocument("443d30c7-dd80-4ac2-9759-1f586b1177c2", DocumentType.MSWORD)),
    JPEG(new EstatetmentDocument("f07db17f-7bf8-4750-b306-862e9a16faaf", DocumentType.JPEG)),
    PDF(new EstatetmentDocument("54fa76ca-9c13-47fd-b39c-5f5cc1887988", DocumentType.PDF)),
    TEXT(new EstatetmentDocument("8a15ca49-af26-4036-a15c-bc74f9bed1b8", DocumentType.TEXT)),
    PDF2(new EstatetmentDocument("YmFja2Jhc2U=", DocumentType.PDF));

    private final EstatetmentDocument eStatetmentDocument;

    private ValidEstatementDocuments(EstatetmentDocument eStatetmentDocument) {
        this.eStatetmentDocument = eStatetmentDocument;
    }

    public EstatetmentDocument geteStatetmentDocument() {
        return eStatetmentDocument;
    }
}
