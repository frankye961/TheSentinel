package com.sentinel.api.model;

import com.sentinel.model.ValidScheme;
import lombok.*;

import java.net.URI;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class AssessmentDTO {
    @ValidScheme
    @NonNull
    private URI assessmentURI;
}
