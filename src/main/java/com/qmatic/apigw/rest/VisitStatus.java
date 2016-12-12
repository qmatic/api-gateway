package com.qmatic.apigw.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class VisitStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer positionInQueue;
    private Integer queueSize;
    private String appointmentTime;
    private Integer waitingTime;
    private String ticketId;
    private Integer totalWaitingTime;
    private Integer queueId;
    private Integer appointmentId;
    private Long visitId;
    private String currentStatus;
    private String queueName;
    private String currentServiceName;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String checksum;

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public Long getVisitId() {
        return visitId;
    }

    public void setVisitId(Long visitId) {
        this.visitId = visitId;
    }

    public Integer getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(Integer waitingTime) {
        this.waitingTime = waitingTime;
    }

    public Integer getTotalWaitingTime() {
        return totalWaitingTime;
    }

    public void setTotalWaitingTime(Integer totalWaitingTime) {
        this.totalWaitingTime = totalWaitingTime;
    }

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public Integer getQueueId() {
        return queueId;
    }

    public void setQueueId(Integer queueId) {
        this.queueId = queueId;
    }

    public void setPositionInQueue(Integer positionInQueue) {
        this.positionInQueue = positionInQueue;
    }

    public void setQueueSize(Integer queueSize) {
        this.queueSize = queueSize;
    }

    public Integer getPositionInQueue() {
        return positionInQueue;
    }

    public Integer getQueueSize() {
        return queueSize;
    }

    public String getCurrentServiceName() {
        return currentServiceName;
    }

    public void setCurrentServiceName(String currentServiceName) {
        this.currentServiceName = currentServiceName;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    @Override
    public String toString() {
        return "VisitStatus{" +
                "positionInQueue=" + positionInQueue +
                ", queueSize=" + queueSize +
                ", appointmentTime='" + appointmentTime + '\'' +
                ", waitingTime=" + waitingTime +
                ", ticketId='" + ticketId + '\'' +
                ", totalWaitingTime=" + totalWaitingTime +
                ", queueId=" + queueId +
                ", appointmentId=" + appointmentId +
                ", visitId=" + visitId +
                ", currentStatus='" + currentStatus + '\'' +
                ", queueName='" + queueName + '\'' +
                ", currentServiceName='" + currentServiceName + '\'' +
                ", checksum='" + checksum + '\'' +
                '}';
    }
}
