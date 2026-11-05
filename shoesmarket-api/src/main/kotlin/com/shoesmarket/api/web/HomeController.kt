package com.shoesmarket.api.web

import com.shoesmarket.infra.persistence.DropEventRepository
import com.shoesmarket.infra.persistence.ProductRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class HomeController(
    private val productRepository: ProductRepository,
    private val dropEventRepository: DropEventRepository,
) {
    @GetMapping("/")
    fun home(model: Model): String {
        model.addAttribute("products", productRepository.findAll())
        return "index"
    }

    @GetMapping("/products/{productId}")
    fun productDetail(
        @PathVariable productId: Long,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        val product = productRepository.findById(productId).orElse(null)
        if (product == null) {
            redirectAttributes.addFlashAttribute("toast", "상품을 찾을 수 없습니다.")
            return "redirect:/"
        }
        model.addAttribute("product", product)
        model.addAttribute("drops", dropEventRepository.findByProductId(productId))
        return "product-detail"
    }
}
