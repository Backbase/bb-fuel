package com.backbase.ct.bbfuel.dto.accountStatement;

public enum ValidEstatementDocuments {

    MSWORD(new EstatetmentDocument("uid_001", DocumentType.MSWORD)),
    JPEG(new EstatetmentDocument("uid_002", DocumentType.JPEG)),
    PDF(new EstatetmentDocument("uid_003", DocumentType.PDF)),
    TEXT(new EstatetmentDocument("uid_004", DocumentType.TEXT)),
    PDF_UID(new EstatetmentDocument("RVNQX0xJTkstaHR0cHM6Ly9zZWN1cmUzLmJpbGxlcndlYi5jb20veGFhL2luZXRTcnY_Y2xpZW50PTcyNDAxMDEwMCZ0eXBlPUxvZ2luTWVudSZhdXRoVGtuPVFRQUFBQUJuTmdEL0ExaEJRUlA0MkJpVGZXSjQvSzBPWjQyT3Uybi9OY1Z3WW9DZ3kxT0FIZVFGWHNqdXlxckRIYjJXaEYyNzlKMVFQVFdjMWE3bmNGczJlekd2VHkzdXlUSGdlamkycnkzL2hBLVE2NFlCQXlSY3ktYjY5a2RpRlRybURJRVoxRUlxRFJ2RzFmYml3blFTc1B2TzFjVUV4V2ZrdGlEMXltZklBOTlNY1ZiNC1zMGFGSC1KcC0vL1NLb3NyQ3JSdjhYR1lEQTI2WmMzMGNJaHRWaGVYMGh6Ym9RSFp2Yjl2SDg2Qm4yVUt0dDIxQ1VNSUFKU3owNnBaNGJQNDNSTlh3bnd2UExDdHpkQ28xMEQ3OGhlVnNQeUczQk11N1FJbmQzOVY1a0ZscXEyQURhOEtZa0UtVFdDeDFhclp5SHhwczUxT0hia1RNSy1qUVV5bXktNlRMdWh5RVN3OTg0XyZ1bml0Q29kZT0yODA", DocumentType.PDF));

    private final EstatetmentDocument eStatetmentDocument;

    private ValidEstatementDocuments(EstatetmentDocument eStatetmentDocument) {
        this.eStatetmentDocument = eStatetmentDocument;
    }

    public EstatetmentDocument geteStatetmentDocument() {
        return eStatetmentDocument;
    }
}
