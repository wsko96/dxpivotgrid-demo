package kr.wise.demo.pivotgrid.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.wise.demo.pivotgrid.model.SaleData;
import kr.wise.demo.pivotgrid.repository.SalesDataRepository;

@RestController
public class SalesDataService {

    @Autowired
    private SalesDataRepository repository;

    @GetMapping("/sales")
    public List<SaleData> all(@RequestParam(name = "limit", required=false, defaultValue="0") int limit) {
        final List<SaleData> list = repository.findAll();
        
        if (limit > 0 && limit < list.size()) {
            return list.subList(0, limit);
        }
        
        return list;
    }

}
