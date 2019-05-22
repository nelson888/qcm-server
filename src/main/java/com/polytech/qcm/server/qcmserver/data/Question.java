package com.polytech.qcm.server.qcmserver.data;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "question")
@Entity
public class Question {

    @Id
    @NonNull
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @NonNull
    @NotBlank
    private String question;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "qcm_id", nullable = false)
    private QCM qcm;

}
