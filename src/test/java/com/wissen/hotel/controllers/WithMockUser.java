package com.wissen.hotel.controllers;

public @interface WithMockUser {

    String username();

    String[] roles();

}
