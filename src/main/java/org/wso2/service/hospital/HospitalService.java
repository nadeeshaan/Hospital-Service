/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.service.hospital;

import com.google.gson.Gson;
import org.wso2.service.hospital.daos.*;
import org.wso2.service.hospital.utils.HospitalUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the Microservice resource class.
 * See <a href="https://github.com/wso2/msf4j#getting-started">https://github.com/wso2/msf4j#getting-started</a>
 * for the usage of annotations.
 *
 * @since 1.0.0-SNAPSHOT
 */
@Path("/hospital/categories")
public class HospitalService {

    private Map<Integer, Appointment> appointments = new HashMap<>();

    public HospitalService() {
        fillCategories();
        HospitalDAO.doctorsList.add((new Doctor("thomas collins", "grand oak community hospital", "surgery", "9.00 a.m - 11.00 a.m", 7000)));
        HospitalDAO.doctorsList.add((new Doctor("henry parker", "grand oak community hospital", "ent", "9.00 a.m - 11.00 a.m", 4500)));
        HospitalDAO.doctorsList.add((new Doctor("abner jones", "grand oak community hospital", "gynaecology", "8.00 a.m - 10.00 a.m", 11000)));
        HospitalDAO.doctorsList.add((new Doctor("abner jones", "grand oak community hospital", "ent", "8.00 a.m - 10.00 a.m", 6750)));
        HospitalDAO.doctorsList.add((new Doctor("anne clement", "clemency medical center", "surgery", "8.00 a.m - 10.00 a.m", 12000)));
        HospitalDAO.doctorsList.add((new Doctor("thomas kirk", "clemency medical center", "gynaecology", "9.00 a.m - 11.00 a.m", 8000)));
        HospitalDAO.doctorsList.add((new Doctor("cailen cooper", "clemency medical center", "paediatric", "9.00 a.m - 11.00 a.m", 5500)));
        HospitalDAO.doctorsList.add((new Doctor("seth mears", "pine valley community hospital", "surgery", "3.00 p.m - 5.00 p.m", 8000)));
        HospitalDAO.doctorsList.add((new Doctor("emeline fulton", "pine valley community hospital", "cardiology", "8.00 a.m - 10.00 a.m", 4000)));
        HospitalDAO.doctorsList.add((new Doctor("jared morris", "willow gardens general hospital", "cardiology", "9.00 a.m - 11.00 a.m", 10000)));
        HospitalDAO.doctorsList.add((new Doctor("henry foster", "willow gardens general hospital", "paediatric", "8.00 a.m - 10.00 a.m", 10000)));
    }

    public void fillCategories() {
        HospitalDAO.catergories.add("surgery");
        HospitalDAO.catergories.add("cardiology");
        HospitalDAO.catergories.add("gynaecology");
        HospitalDAO.catergories.add("ent");
        HospitalDAO.catergories.add("paediatric");
    }

    @POST
    @Path("/{category}/reserve")
    public Response reserveAppointment(AppointmentRequest appointmentRequest, @PathParam("category") String category) {

        // Check whether the requested category available
        if (HospitalDAO.catergories.contains(category)) {
            Appointment appointment = HospitalUtil.makeNewAppointment(appointmentRequest);

            if (appointment == null) {
                Status status = new Status("Doctor "+ appointmentRequest.getDoctor() + " isn't available in " +
                        appointmentRequest.getHospital());
                return Response.status(Response.Status.OK) .entity(status).type(MediaType.APPLICATION_JSON).build();
            }

            this.appointments.put(appointment.getAppointmentNumber(), appointment);
            HospitalDAO.patientMap.put(appointmentRequest.getPatient().getSsn(), appointmentRequest.getPatient());
            if (!HospitalDAO.patientRecordMap.containsKey(appointmentRequest.getPatient().getSsn())) {
                PatientRecord patientRecord = new PatientRecord(appointmentRequest.getPatient());
                HospitalDAO.patientRecordMap.put(appointmentRequest.getPatient().getSsn(), patientRecord);
            }

            return Response.status(Response.Status.OK) .entity(appointment).type(MediaType.APPLICATION_JSON).build();
        } else {
            // Cannot find a doctor for this category
            Status status = new Status("Invalid Category");
            return Response.ok(status, MediaType.APPLICATION_JSON).build();
        }
    }

    @GET
    @Path("/appointments/{appointment_id}/fee")
    public Response checkChannellingFee(@PathParam("appointment_id") int id) {
        //Check for the appointment number validity
        ChannelingFeeDao channelingFee = new ChannelingFeeDao();
        if (appointments.containsKey(id)) {
            Patient patient = appointments.get(id).getPatient();
            Doctor doctor = appointments.get(id).getDoctor();

            channelingFee.setActualFee(Double.toString(doctor.getFee()));
            channelingFee.setDoctorName(doctor.getName().toLowerCase());
            channelingFee.setPatientName(patient.getName().toLowerCase());

            return Response.status(Response.Status.OK) .entity(channelingFee).type(MediaType.APPLICATION_JSON).build();
        } else {
            Status status = new Status("Error.Could not Find the Requested appointment ID");
            return Response.status(Response.Status.OK) .entity(status).type(MediaType.APPLICATION_JSON).build();
        }
    }

    @POST
    @Path("/patient/updaterecord")
    public Response updatePatientRecord(HashMap<String,Object> patientDetails) {
        String SSN = (String)patientDetails.get("SSN");
        List symptoms = (List)patientDetails.get("symptoms");
        List treatments = (List)patientDetails.get("treatments");

        if (HospitalDAO.patientMap.get(SSN) != null) {
            Patient patient = HospitalDAO.patientMap.get(SSN);
            PatientRecord patientRecord = HospitalDAO.patientRecordMap.get(SSN);
            if (patient != null) {
                patientRecord.updateSymptoms(symptoms);
                patientRecord.updateTreatments(treatments);
                Status status =new Status("Record Update Success");
                return Response.status(Response.Status.OK) .entity(status).type(MediaType.APPLICATION_JSON).build();
            } else {
                Status status =new Status("Could not find valid Patient Record");
                return Response.status(Response.Status.OK) .entity(status).type(MediaType.APPLICATION_JSON).build();
            }
        } else {
            Status status =new Status("Could not find valid Patient Entry");
            return Response.status(Response.Status.OK).entity(status).type(MediaType.APPLICATION_JSON).build();
        }
    }

    @GET
    @Path("/patient/{SSN}/getrecord")
    public Response getPatientRecord(@PathParam("SSN") String SSN) {
        PatientRecord patientRecord = HospitalDAO.patientRecordMap.get(SSN);

        if (patientRecord == null) {
            Status status =new Status("Could not find valid Patient Entry");
            return Response.status(Response.Status.OK).entity(status).type(MediaType.APPLICATION_JSON).build();
        } else {
            return Response.status(Response.Status.OK).entity(patientRecord).type(MediaType.APPLICATION_JSON).build();
        }
    }

    @GET
    @Path("/patient/appointment/{appointment_id}/discount")
    public Response isEligibleForDiscount(@PathParam("appointment_id") int id) {
        Appointment appointment = appointments.get(id);
        if (appointment == null) {
            Status status =new Status("Invalid appointment ID");
            return Response.status(Response.Status.OK).entity(status).type(MediaType.APPLICATION_JSON).build();
        } else {
            boolean eligible = HospitalUtil.checDiscountEligibility(appointment.getPatient().getDob());
            Status status = new Status(String.valueOf(eligible));
            return Response.status(Response.Status.OK).entity(status).type(MediaType.APPLICATION_JSON).build();
        }
    }
}
