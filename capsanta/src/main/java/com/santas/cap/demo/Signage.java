package com.santas.cap.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;


@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Signage {

    @GeneratedValue
    @Id
    Long id;
    String docId;
    String signerId;
    Long timeInMillis;
}
