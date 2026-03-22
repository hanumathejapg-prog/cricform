package com.cricform.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalPlayerProfileDTO {
    private Long id;
    private String name;
    private String role;
    private Long teamId;
    private String teamName;
}
