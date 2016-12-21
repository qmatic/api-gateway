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
    private String staffFirstName;
    private String staffLastName;
    private Integer servicePointLogicId;
    private String servicePointName;
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

    // Myfunwait expects the positionInQueue field to be named "position"
    public Integer getPosition() {
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

    public String getStaffFirstName() {
        return staffFirstName;
    }

    public void setStaffFirstName(String staffFirstName) {
        this.staffFirstName = staffFirstName;
    }

    public String getStaffLastName() {
        return staffLastName;
    }

    public void setStaffLastName(String staffLastName) {
        this.staffLastName = staffLastName;
    }

    public Integer getServicePointLogicId() {
        return servicePointLogicId;
    }

    public void setServicePointLogicId(Integer servicePointLogicId) {
        this.servicePointLogicId = servicePointLogicId;
    }

    public String getServicePointName() {
        return servicePointName;
    }

    public void setServicePointName(String servicePointName) {
        this.servicePointName = servicePointName;
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
                ", staffFirstName='" + staffFirstName + '\'' +
                ", staffFirstName='" + staffLastName + '\'' +
                ", servicePointLogicId='" + servicePointLogicId + '\'' +
                ", servicePointName='" + servicePointName + '\'' +
                ", checksum='" + checksum + '\'' +
                '}';
    }
}
