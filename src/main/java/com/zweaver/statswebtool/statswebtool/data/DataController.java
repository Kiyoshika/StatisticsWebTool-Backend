package com.zweaver.statswebtool.statswebtool.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.zweaver.statswebtool.statswebtool.data.filter.FilterData;
import com.zweaver.statswebtool.statswebtool.data.sort.SortData;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = {"http://localhost:8080", "https://statisticswebtool.netlify.app/"})
public class DataController {
    // file name -- data object
    private HashMap<String, Data> dataStore = new HashMap<>();

    /*
     * Import CSV file Client: Nav Bar --> Data --> Import CSV File
     */
    @RequestMapping(value = "/importData", method = RequestMethod.POST)
    public void importCSV(@RequestParam("file") MultipartFile file, @RequestHeader("client-username") String username)
            throws IOException {
        Data newDataObject = new Data();

        InputStream inputFileStream = file.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputFileStream));
        newDataObject.getDims(br);

        // reinitialize buffered reader for second iteration
        InputStream inputFileStream2 = file.getInputStream();
        BufferedReader br2 = new BufferedReader(new InputStreamReader(inputFileStream2));
        newDataObject.parseCSV(br2);

        dataStore.put(username + "::" + file.getOriginalFilename(), newDataObject);
    }

    /*
     * Get CSV files for current user and populate "dataset list" on client
     */
    @RequestMapping(value = "/getData/{fileName}", method = RequestMethod.GET)
    public Map<Object, Object> getDatasetList(@RequestHeader("client-username") String username,
            @PathVariable("fileName") String fileName) {
        // filter data store by username of client and file name, then return map of file name (keys) with data object (values)
        return dataStore.entrySet().stream().filter(map -> map.getKey().substring(0, map.getKey().indexOf("::")).equals(username) 
        && map.getKey().substring(map.getKey().indexOf("::") + 2, map.getKey().length()).equals(fileName))
        .collect(Collectors.toMap(map -> map.getKey().substring(map.getKey().indexOf("::")+2, map.getKey().length()), map -> map.getValue()));
    }

    /*
    * Remove/delete a dataset that's been uploaded
    */
    @RequestMapping(value = "/removeData/{fileName}", method = RequestMethod.POST)
    public void removeDataset(@RequestHeader("client-username") String username,
        @PathVariable("fileName") String fileName) {
        
        if (dataStore.get(username + "::" + fileName) != null) {
            dataStore.remove(username + "::" + fileName);
        }
    }

    /*
    * Rename a dataset
    */
    @RequestMapping(value = "/renameData/{oldFileName}/{newFileName}", method = RequestMethod.POST)
    public void renameDataset(@RequestHeader("client-username") String username,
        @PathVariable("oldFileName") String oldFileName,
        @PathVariable("newFileName") String newFileName) {

        if (dataStore.get(username + "::" + oldFileName) != null) {
            dataStore.put(username + "::" + newFileName, dataStore.get(username + "::" + oldFileName));
            dataStore.remove(username + "::" + oldFileName);
        }
    }

    /*
    * Get all uploaded file names for a user
    */
    @RequestMapping(value = "/getUploadedFileNames", method = RequestMethod.GET)
    public List<Object> getUploadedFileNames(@RequestHeader("client-username") String username) {
        List<Object> uploadedDatasets = dataStore.keySet().stream().filter(mapKey -> mapKey.substring(0, mapKey.indexOf("::")).equals(username))
        .collect(Collectors.toList());

        // remove the username from dataset name
        uploadedDatasets.replaceAll(s -> s.toString().substring(s.toString().indexOf("::") + 2, s.toString().length()));
        return uploadedDatasets;
    }

    /*
    * Filter a dataset
    */
    @RequestMapping(value = "/filterData/{datasetName}", method = RequestMethod.POST)
    public Data filterData(@RequestHeader("client-username") String username,
        @PathVariable("datasetName") String datasetName,
        @RequestBody String[][] filterConditions) {
            // get current dataset
            Data currentData = dataStore.get(username + "::" + datasetName);
            FilterData fd = new FilterData();
            fd.setColumns(currentData.getColumns());

            Data filteredData = new Data();
            filteredData.setData(fd.filter(currentData.getData(), filterConditions));
            filteredData.setColumns(currentData.getColumns());

            return filteredData;
            
    }

    /*
    * Sort a dataset
    */
    @RequestMapping(value = "/sortData/{datasetName}", method = RequestMethod.POST)
    public Data sortData(@RequestHeader("client-username") String username,
        @PathVariable("datasetName") String datasetName,
        @RequestBody String[][] sortConditions) {
            // get current dataset
            Data currentData = dataStore.get(username + "::" + datasetName);
            SortData sd = new SortData();
            sd.setColumns(currentData.getColumns());

            Data sortedData = new Data();
            sortedData.setData(sd.sortData(currentData.getData(), sortConditions));
            sortedData.setColumns(currentData.getColumns());

            return sortedData;

        }
}
