package com.zweaver.statswebtool.statswebtool.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class DataController {
    // file name -- data object
    private HashMap<String, Data> dataStore = new HashMap<>();

    /*
     * Import CSV file Client: Nav Bar --> Data --> Import CSV File
     */
    @RequestMapping(value = "/importData", method = RequestMethod.POST)
    public void importCSV(@RequestParam("file") MultipartFile file, @RequestHeader("client-username") String username) {
        // 0 - file path
        // 1 - file name
        String[] fileUploadReturns = FileService.uploadFile(file);
        Data newDataObject = new Data();
        newDataObject.parseCSV(fileUploadReturns[0]);
        dataStore.put(username + "::" + fileUploadReturns[1], newDataObject);
    }

    /*
     * Get CSV files for current user and populate "dataset list" on client
     */
    @RequestMapping(value = "/getData", method = RequestMethod.GET)
    public Map<Object, Object> getDatasetList(@RequestHeader("client-username") String username) {
        // filter data store by username of client and return map of file name (keys) with data object (values)
        return dataStore.entrySet().stream().filter(map -> map.getKey().substring(0, map.getKey().indexOf("::")).contains(username))
        .collect(Collectors.toMap(map -> map.getKey().substring(map.getKey().indexOf("::")+2, map.getKey().length()), map -> map.getValue()));
    }
}
