package com.example.board;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class TicketController {

    private final TicketRepository repo;

    public TicketController(TicketRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/tickets";
    }

    @GetMapping("/tickets")
    public String list(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("tickets", repo.findAll(q));
        model.addAttribute("q", q == null ? "" : q);
        return "list";
    }

    @GetMapping("/tickets/new")
    public String newForm(Model model) {
        model.addAttribute("ticket", new Ticket());
        return "form";
    }

    @PostMapping("/tickets")
    public String create(@ModelAttribute Ticket ticket) {
        repo.insert(normalize(ticket));
        return "redirect:/tickets";
    }

    @GetMapping("/tickets/{id}")
    public String detail(@PathVariable long id, Model model) {
        Ticket t = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "없는 접수 번호입니다: " + id));
        model.addAttribute("ticket", t);
        return "form";
    }

    @PostMapping("/tickets/{id}")
    public String update(@PathVariable long id, @ModelAttribute Ticket ticket) {
        if (repo.update(id, normalize(ticket)) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "없는 접수 번호입니다: " + id);
        }
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/tickets/{id}/delete")
    public String delete(@PathVariable long id) {
        repo.delete(id);
        return "redirect:/tickets";
    }

    /** 빈 값 방어: 화면의 required 는 우회할 수 있으므로 서버에서도 한 번 더 확인 */
    private Ticket normalize(Ticket t) {
        if (t.getTitle() == null || t.getTitle().isBlank()) {
            t.setTitle("(제목 없음)");
        }
        if (t.getServerName() == null || t.getServerName().isBlank()) {
            t.setServerName("-");
        }
        if (t.getStatus() == null || t.getStatus().isBlank()) {
            t.setStatus("OPEN");
        }
        return t;
    }
}
