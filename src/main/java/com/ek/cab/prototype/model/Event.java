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
public class Event implements Serializable {

    private String eventId;
    private String producer;
    private String action;
    private String resourceId;
    private Data data;
}







