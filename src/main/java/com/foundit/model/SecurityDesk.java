package com.foundit.model;

import jakarta.persistence.*;

@Entity
@Table(name = "security_desks")
public class SecurityDesk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 200)
    private String location;

    @Column(length = 15)
    private String contactNumber;

    @Column(length = 100)
    private String operatingHours;

    public Integer getId() { return this.id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return this.location; }
    public void setLocation(String location) { this.location = location; }

    public String getContactNumber() { return this.contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getOperatingHours() { return this.operatingHours; }
    public void setOperatingHours(String operatingHours) { this.operatingHours = operatingHours; }
}
