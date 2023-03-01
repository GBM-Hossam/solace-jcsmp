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
public class Data implements Serializable {
    private String user_Agent;
    private String eK_SiteCountry;
    private String eK_CorrelationId;
    private String eK_ChannelName;
    private String eK_RequestId;
    private String eK_ClientIP;
    private String eK_ApplicationCode;
    private String eK_SiteLocale;
    private String eK_AppServer;
}