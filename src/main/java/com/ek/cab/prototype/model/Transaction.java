package com.ek.cab.prototype.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Transaction implements Serializable {

    private String id;
    private String from;
    private String to;

    public Transaction(final String from, final String to) {
        this.from = from;
        this.to = to;
    }
}
