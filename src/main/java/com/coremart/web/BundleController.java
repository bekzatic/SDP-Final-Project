package com.coremart.web;

import com.coremart.bundle.BundleCatalog;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Page with the ready-made bundles (Composite). */
@Controller
public class BundleController {

    private final BundleCatalog bundles;

    public BundleController(BundleCatalog bundles) {
        this.bundles = bundles;
    }

    @GetMapping("/bundles")
    public String bundles(Model model) {
        model.addAttribute("bundles", bundles.getAll());
        return "bundles";
    }
}
