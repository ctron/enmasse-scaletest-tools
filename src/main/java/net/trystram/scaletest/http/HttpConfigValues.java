package net.trystram.scaletest.http;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import net.trystram.util.BaseConfigValues;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HttpConfigValues extends BaseConfigValues {


    private long numberOfDevicesToCreate;
    private String csvLogFile;
    private String createdIdsFile;
    private int logInterval = 10;

    private int port = 443;
    @JsonProperty("authToken")
    private String password;

    public long getNumberOfDevicesToCreate() {
        return numberOfDevicesToCreate;
    }

    public void setNumberOfDevicesToCreate(long numberOfDevicesToCreate) {
        this.numberOfDevicesToCreate = numberOfDevicesToCreate;
    }

    public String getCsvLogFile() {
        return csvLogFile;
    }

    public void setCsvLogFile(String csvLogFile) {
        this.csvLogFile = csvLogFile;
    }

    public String getCreatedIdsFile() {
        return createdIdsFile;
    }

    public void setCreatedIdsFile(String createdIdsFile) {
        this.createdIdsFile = createdIdsFile;
    }

    public int getLogInterval() {
        return logInterval;
    }

    public void setLogInterval(int logInterval) {
        this.logInterval = logInterval;
    }

    public String verify(){

        ArrayList<String> missingValues = new ArrayList<>();

        if (this.getTenantId() == null) {
            missingValues.add("iotProject ");
        }
        if (this.getHost() == null) {
            missingValues.add("host");
        }
        if (this.getPassword() == null) {
            missingValues.add("authToken");
        }

        if (missingValues.size() != 0){
            final String message = "Missing configuration value(s): ";
            return message + String.join(", ", missingValues);
        } else {
            return null;
        }
    }
}