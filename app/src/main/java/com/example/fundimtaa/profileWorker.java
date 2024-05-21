package com.example.fundimtaa;

public class profileWorker {

        private String name;
        private String email;
        private String phoneNumber;
        private String location;
        private  String experience;
        private String specialization;
        // Add more fields as needed

        public profileWorker() {
            // Default constructor required for Firestore
        }

        public profileWorker(String name, String email, String phoneNumber, String location,String experience,
                             String specialization) {
            this.name = name;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.location = location;
            this.experience = experience;
            this.specialization = specialization;

        }

        // Getters and setters for each field
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}


