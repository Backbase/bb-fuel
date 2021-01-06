package com.backbase.ct.bbfuel.dto.accountStatement;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Accessors(chain = true)
@Getter
@Setter
@AllArgsConstructor
public class EstatementPostRequestBody {

    private String accountId;
    private String userId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date date;
    private String accountNumber;
    private String accountName;
    private Categories category;
    private String description;
    private List<EstatetmentDocument> documents;
    private Map<String, String> additions;

    public EstatementPostRequestBody() {
    }
}
