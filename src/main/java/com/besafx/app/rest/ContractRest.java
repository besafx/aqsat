package com.besafx.app.rest;

import com.besafx.app.config.CustomException;
import com.besafx.app.entity.Person;
import com.besafx.app.entity.Contract;
import com.besafx.app.service.PersonService;
import com.besafx.app.service.ContractService;
import com.besafx.app.util.JSONConverter;
import com.besafx.app.util.Options;
import com.besafx.app.ws.Notification;
import com.besafx.app.ws.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(value = "/api/contract/")
public class ContractRest {

    private final static Logger log = LoggerFactory.getLogger(ContractRest.class);

    public static final String FILTER_TABLE = "**,customer[id,code,name],supplier[id,code,name],lastPerson[id,name]";
    public static final String FILTER_CONTRACT_COMBO = "id,code,name,mobile";

    @Autowired
    private ContractService contractService;

    @Autowired
    private PersonService personService;

    @Autowired
    private NotificationService notificationService;

    @RequestMapping(value = "create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_CONTRACT_CREATE')")
    public String create(@RequestBody Contract contract, Principal principal) {
        Contract topContract = contractService.findTopByOrderByCodeDesc();
        if (topContract == null) {
            contract.setCode(Long.valueOf(1));
        } else {
            contract.setCode(topContract.getCode() + 1);
        }
        Person caller = personService.findByEmail(principal.getName());
        contract.setLastUpdate(new DateTime().toDate());
        contract.setLastPerson(caller);
        contract = contractService.save(contract);
        String lang = JSONConverter.toObject(caller.getOptions(), Options.class).getLang();
        notificationService.notifyOne(Notification.builder().message(lang.equals("AR") ? "تم انشاء حساب التاجر بنجاح" : "Create Contract Account Successfully").type("success").icon("fa-plus-square").build(), principal.getName());
        return SquigglyUtils.stringify(Squiggly.init(new ObjectMapper(), FILTER_TABLE), contract);
    }

    @RequestMapping(value = "update", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_CONTRACT_UPDATE')")
    public String update(@RequestBody Contract contract, Principal principal) {
        if (contractService.findByCodeAndIdIsNot(contract.getCode(), contract.getId()) != null) {
            throw new CustomException("هذا الكود مستخدم سابقاً، فضلاً قم بتغير الكود.");
        }
        Contract object = contractService.findOne(contract.getId());
        if (object != null) {
            Person caller = personService.findByEmail(principal.getName());
            contract.setLastUpdate(new DateTime().toDate());
            contract.setLastPerson(caller);
            contract = contractService.save(contract);
            String lang = JSONConverter.toObject(caller.getOptions(), Options.class).getLang();
            notificationService.notifyOne(Notification.builder().message(lang.equals("AR") ? "تم تعديل بيانات حساب التاجر بنجاح" : "Update Contract Account Information Successfully").type("warning").icon("fa-edit").build(), principal.getName());
            return SquigglyUtils.stringify(Squiggly.init(new ObjectMapper(), FILTER_TABLE), contract);
        } else {
            return null;
        }
    }

    @RequestMapping(value = "delete/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_CONTRACT_DELETE')")
    public void delete(@PathVariable Long id, Principal principal) {
        Contract contract = contractService.findOne(id);
        if (contract != null) {
            contractService.delete(id);
            Person caller = personService.findByEmail(principal.getName());
            String lang = JSONConverter.toObject(caller.getOptions(), Options.class).getLang();
            notificationService.notifyOne(Notification.builder().message(lang.equals("AR") ? "تم حذف حساب التاجر وكل ما يتعلق به من حسابات بنجاح" : "Delete Contract Account With All Related Successfully").type("error").icon("fa-trash").build(), principal.getName());
        }
    }

    @RequestMapping(value = "findAll", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String findAll() {
        List<Contract> list = Lists.newArrayList(contractService.findAll());
        list.sort(Comparator.comparing(Contract::getCode));
        return SquigglyUtils.stringify(Squiggly.init(new ObjectMapper(), FILTER_TABLE), list);
    }

    @RequestMapping(value = "findAllCombo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String findAllCombo() {
        List<Contract> list = Lists.newArrayList(contractService.findAll());
        list.sort(Comparator.comparing(Contract::getCode));
        return SquigglyUtils.stringify(Squiggly.init(new ObjectMapper(), FILTER_CONTRACT_COMBO), list);
    }

    @RequestMapping(value = "findOne/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String findOne(@PathVariable Long id) {
        return SquigglyUtils.stringify(Squiggly.init(new ObjectMapper(), FILTER_TABLE), contractService.findOne(id));
    }
}
