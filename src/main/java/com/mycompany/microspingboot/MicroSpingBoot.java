/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.microspingboot;

import com.mycompany.httpserver.HttpServer;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

/**
 *
 * @author juan.parroquiano
 */
public class MicroSpingBoot {

    public static void main(String[] args) throws IOException, URISyntaxException, InvocationTargetException {
        System.out.println("Starting MicroSpringBoot");
        
        HttpServer.runServer(args);
    }
}
