package org.wso2.service.hospital.daos;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by nadeeshaan on 7/20/16.
 */

@XmlRootElement
public class AppointmentRequest {
    private Patient patient;
    private String doctor;

    public AppointmentRequest(Patient patient, String doctor) {
        this.patient = patient;
        this.doctor = doctor;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getDoctor() {
        return doctor;
    }

    public void setDoctor(String doctor) {
        this.doctor = doctor;
    }
}
