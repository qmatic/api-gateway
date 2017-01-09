/**
 * Copyright 2011 Q-Matic AB.
 */
package com.qmatic.common.geo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Johan Grönvall (johan.gronvall@cybercomgroup.com)
 */
@XmlRootElement
public class Branch implements Serializable {

   private static final Logger log = LoggerFactory.getLogger(Branch.class);

   private static final long serialVersionUID = 6948366187913810001L;
   private long id;
   private String name;
   private String addressLine1;
   private String addressLine2;
   private String addressLine3;
   private String addressLine4;
   private String addressLine5;
   private String addressPostalCode;
   private String timeZone;
   private double longitude;
   private double latitude;
   private String openTime;
   private String closeTime;
   private Date suggestedTime = new Date(0);
   private String message = "";
   private int estimatedWaitTime;
   private boolean branchOpen = true;
   private boolean queuePassesClosingTime = false;

   public Branch() {
   } // Makes JAX-RS happy.

   /**
    * The longitude of this branch as micro degree.
    *
    * @return The longitude of this branch.
    */
   @XmlElement
   public int getLongitudeE6() {
      return microDegrees(longitude);
   }

   /**
    * The latitude of this branch as micro degree.
    *
    * @return The latitude of this branch.
    */
   @XmlElement
   public int getLatitudeE6() {
      return microDegrees(latitude);
   }

   /**
    * The longitude of this branch.
    *
    * @return The longitude of this branch.
    */
   @XmlElement
   public double getLongitude() {
      return longitude;
   }

   @XmlElement
   public double getLatitude() {
      return latitude;
   }

   @XmlElement
   public long getId() {
      return id;
   }

   /**
    * The opening time for this branch in the format HH:mm. Example "10:00".
    */
   @XmlElement
   public String getOpenTime() {
      return openTime;
   }

   /**
    * The closing time for this branch in the format HH:mm. Example "18:00".
    */
   @XmlElement
   public String getCloseTime() {
      return closeTime;
   }

   @XmlElement
   public String getName() {
      return name;
   }

   @XmlElement
   public String getAddressLine1() {
      return addressLine1;
   }

   @XmlElement
   public String getAddressLine2() {
      return addressLine2;
   }

   @XmlElement
   public String getAddressLine3() {
      return addressLine3;
   }

   @XmlElement
   public String getAddressLine4() {
      return addressLine4;
   }

   @XmlElement
   public String getAddressLine5() {
      return addressLine5;
   }

   @XmlElement
   public String getAddressPostalCode() {
      return addressPostalCode;
   }

   /**
    * The time zone of this branch in the Java TimeZone ID format, e.g. "Pacific/Midway", "Europe/Stockholm".
    *
    * @return The time zone of this branch.
    */
   @XmlElement
   public String getTimeZone() {
      return timeZone;
   }

   public void setSuggestedTime(Date suggestedTime) {
      this.suggestedTime = (Date) suggestedTime.clone();
   }

   @XmlTransient
   public Date getSuggestedTime() {
      return suggestedTime == null ? null : (Date) suggestedTime.clone();
   }

   /**
    * The message (commercial or informative) to display on ticket.
    *
    * @return
    */
   @XmlTransient
   public String getMessage() {
      return message;
   }

   public void setMessage(String message) {
      this.message = message;
   }

   /**
    * The estimated waiting time in minutes.
    *
    * @return The estimated waiting time in minutes.
    */
   public int getEstimatedWaitTime() {
      return estimatedWaitTime;
   }

   /**
    * Sets the estimated waiting time in seconds.
    *
    * @param estimatedWaitTime The estimated waiting time in seconds.
    */
   public void setEstimatedWaitTime(int estimatedWaitTime) {
      this.estimatedWaitTime = Math.round((float) estimatedWaitTime / 60);
   }

   /**
    * Indicates if a branch is open or not. This helps the client to decide if it's possible to issue a ticket or not.
    *
    * @return True if open, false if closed.
    */
   @XmlElement
   public boolean isBranchOpen() {
      return branchOpen;
   }

   /**
    * Indicates if a queue waiting time passes close time.
    *
    * @return True if it passes waiting time, false otherwise.
    */
   @XmlElement
   public boolean isQueuePassesClosingTime() {
      return queuePassesClosingTime;
   }

   public void setQueuePassesClosingTime(boolean queuePassesClosingTime) {
      this.queuePassesClosingTime = queuePassesClosingTime;
   }

   public void setBranchOpen(boolean branchOpen) {
      this.branchOpen = branchOpen;
   }

   /**
    * Inner class used to construct Branch objects.
    *
    * @author jogl
    */
   public static final class Builder {

      private long id;
      private String name;
      private String addressLine1;
      private String addressLine2;
      private String addressLine3;
      private String addressLine4;
      private String addressLine5;
      private String addressPostalCode;
      private String timeZone;
      private double longitude;
      private double latitude;
      private String openTime;
      private String closeTime;
      private int estimatedWaitTime;

      public Builder id(final long id) {
         this.id = id;
         return this;
      }

      public Builder name(final String name) {
         this.name = name;
         return this;
      }

      public Builder addressLine1(final String addressLine1) {
         this.addressLine1 = addressLine1;
         return this;
      }

      public Builder addressLine2(final String addressLine2) {
         this.addressLine2 = addressLine2;
         return this;
      }

      public Builder addressLine3(final String addressLine3) {
         this.addressLine3 = addressLine3;
         return this;
      }

      public Builder addressLine4(final String addressLine4) {
         this.addressLine4 = addressLine4;
         return this;
      }

      public Builder addressLine5(final String addressLine5) {
         this.addressLine5 = addressLine5;
         return this;
      }

      public Builder addressPostalCode(final String addressPostalCode) {
         this.addressPostalCode = addressPostalCode;
         return this;
      }

      public Builder timeZone(final String timeZone) {
         this.timeZone = timeZone;
         return this;
      }

      public Builder longitude(final double degrees) {
         this.longitude = degrees;
         return this;
      }

      public Builder latitude(final double degrees) {
         this.latitude = degrees;
         return this;
      }

      public Builder openTime(final String openTime) {
         this.openTime = openTime;
         return this;
      }

      public Builder closeTime(final String closeTime) {
         this.closeTime = closeTime;
         return this;
      }

      public Branch build() {
         return new Branch(this);
      }

      public Builder estimatedWaitTime(int estimatedWaitTime) {
         this.estimatedWaitTime = estimatedWaitTime;
         return this;
      }
   }

   private Branch(final Builder builder) {
      this.name = builder.name;
      this.id = builder.id;
      this.addressLine1 = builder.addressLine1;
      this.addressLine2 = builder.addressLine2;
      this.addressLine3 = builder.addressLine3;
      this.addressLine4 = builder.addressLine4;
      this.addressLine5 = builder.addressLine5;
      this.addressPostalCode = builder.addressPostalCode;
      this.timeZone = builder.timeZone;
      this.openTime = formatTimeAsHHmm(builder.openTime);
      this.closeTime = formatTimeAsHHmm(builder.closeTime);
      this.longitude = builder.longitude;
      this.latitude = builder.latitude;
      this.estimatedWaitTime = builder.estimatedWaitTime;
   }

   private int microDegrees(final double degrees) {
      return (int) (degrees * 1E6);
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (int) (id ^ (id >>> 32));
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {

         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      Branch other = (Branch) obj;
      if (id != other.id) {
         return false;
      }
      return true;
   }

   /**
    * Compare all attributes, not just the ID
    *
    * @param other
    * @return
    */
   public boolean fullEquals(Branch other) {
      if (id != other.id) {
         return false;
      }
      if ((name == null) ? (other.name != null) : !name.equals(other.name)) {
         return false;
      }
      if ((addressLine1 == null) ? (other.addressLine1 != null) : !addressLine1.equals(other.addressLine1)) {
         return false;
      }
      if ((addressLine2 == null) ? (other.addressLine2 != null) : !addressLine2.equals(other.addressLine2)) {
         return false;
      }
      if ((addressLine3 == null) ? (other.addressLine3 != null) : !addressLine3.equals(other.addressLine3)) {
         return false;
      }
      if ((addressLine4 == null) ? (other.addressLine4 != null) : !addressLine4.equals(other.addressLine4)) {
         return false;
      }
      if ((addressLine5 == null) ? (other.addressLine5 != null) : !addressLine5.equals(other.addressLine5)) {
         return false;
      }
      if ((addressPostalCode == null) ? (other.addressPostalCode != null) : !addressPostalCode.equals(other.addressPostalCode)) {
         return false;
      }
      if ((timeZone == null) ? (other.timeZone != null) : !timeZone.equals(other.timeZone)) {
         return false;
      }
      if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude)) {
         return false;
      }
      if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude)) {
         return false;
      }
      if ((openTime == null) ? (other.openTime != null) : !openTime.equals(other.openTime)) {
         return false;
      }
      if ((closeTime == null) ? (other.closeTime != null) : !closeTime.equals(other.closeTime)) {
         return false;
      }
      if (suggestedTime != other.suggestedTime && (suggestedTime == null || !suggestedTime.equals(other.suggestedTime))) {
         return false;
      }
      if ((message == null) ? (other.message != null) : !message.equals(other.message)) {
         return false;
      }
      if (estimatedWaitTime != other.estimatedWaitTime) {
         return false;
      }
      if (branchOpen != other.branchOpen) {
         return false;
      }
      if (queuePassesClosingTime != other.queuePassesClosingTime) {
         return false;
      }
      return true;
   }

   public static String formatTimeAsHHmm(final String time) {
      final SimpleDateFormat format = new SimpleDateFormat("HH:mm");

      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      Date timeAsDate = cal.getTime();

      if (time != null && ! time.isEmpty()) {
         try {
            timeAsDate = new SimpleDateFormat("HH:mm:ss").parse(time);
         } catch (ParseException ex) {
            log.warn("Could not parse time string as HH:mm:ss, {}", ex.getMessage());
            try {
               timeAsDate = format.parse(time);
            } catch (ParseException e) {
               log.error("Could not parse time string as HH:mm either, {}", e.getMessage());
            }
         }
      }

      return format.format(timeAsDate);
   }

}
