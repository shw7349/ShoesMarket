package com.shoesmarket.api.web

import com.shoesmarket.infra.persistence.DropEventRepository
import com.shoesmarket.infra.persistence.ProductRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class DropWebController(
    private val dropEventRepository: DropEventRepository,
    private val productRepository: ProductRepository,
) {
    @GetMapping("/drops/{dropId}")
    fun dropDetail(
        @PathVariable dropId: Long,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        val drop = dropEventRepository.findById(dropId).orElse(null)
        if (drop == null) {
            redirectAttributes.addFlashAttribute("toast", "드롭을 찾을 수 없습니다.")
            return "redirect:/"
        }
        model.addAttribute("drop", drop)
        model.addAttribute("product", productRepository.findById(drop.productId).orElse(null))
        return "drop-detail"
    }
}
