package com.romashka.romashka_telecom;

import com.romashka.romashka_telecom.config.RabbitMQConfig;
import com.romashka.romashka_telecom.repository.CdrDataRepository;
import com.romashka.romashka_telecom.service.CallsGenerationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@Import(RabbitMQConfig.class)
public class RomashkaTelecomApplication {

	public static void main(String[] args) {

		var ctx = SpringApplication.run(RomashkaTelecomApplication.class, args);
		var repo = ctx.getBean(CdrDataRepository.class);
		// Запускаем валидацию
//		validateSessions(repo);
	}

	@Bean
	public CommandLineRunner demo(CallsGenerationService callsService) {
		return args -> {
			System.out.println("=== Starting calls generation and export ===");
			callsService.generateCalls();
			System.out.println("=== Process completed ===");
		};
	}
}






//	/**
//	 * Читает все CDR из репозитория и проверяет:
//	 * 1) все записи в одном дне (no crossing midnight),
//	 * 2) никакие интервалы у одного номера не пересекаются.
//	 */
//	private static void validateSessions(CdrDataRepository repo) {
//		List<CdrData> all = repo.findAll();
//
//		// Сначала группируем звонки по их парам (исходящий + входящий)
//		Map<Long, List<CdrData>> callsByPair = new HashMap<>();
//		for (CdrData c : all) {
//			// Используем call_id как ключ для группировки пар
//			callsByPair.computeIfAbsent(c.getCallId(), k -> new ArrayList<>()).add(c);
//		}
//
//		// Группируем только исходящие звонки по номеру абонента
//		Map<String, List<CdrData>> outgoingByNumber = new HashMap<>();
//		for (List<CdrData> pair : callsByPair.values()) {
//			// Берем только исходящий звонок из пары
//			CdrData outgoing = pair.stream()
//					.filter(c -> c.getCallType() == CallType.OUTGOING)
//					.findFirst()
//					.orElse(null);
//
//			if (outgoing != null) {
//				outgoingByNumber.computeIfAbsent(outgoing.getCallerNumber(),
//						k -> new ArrayList<>()).add(outgoing);
//			}
//		}
//
//		boolean valid = true;
//
//		// Проверяем только исходящие звонки
//		for (var entry : outgoingByNumber.entrySet()) {
//			String number = entry.getKey();
//			List<CdrData> calls = entry.getValue();
//
//			// Сортируем по времени начала
//			calls.sort(Comparator.comparing(CdrData::getStartTime));
//
//			LocalDateTime prevEnd = null;
//			for (CdrData c : calls) {
//				// 1) Проверка на пересечение полночи
//				if (!c.getStartTime().toLocalDate().equals(c.getEndTime().toLocalDate())) {
//					System.err.printf(
//							"❌ Session crosses midnight for %s: %s → %s%n",
//							number, c.getStartTime(), c.getEndTime()
//					);
//					valid = false;
//				}
//
//				// 2) Проверка пересечений интервалов
//				if (prevEnd != null && c.getStartTime().isBefore(prevEnd)) {
//					System.err.printf(
//							"❌ REAL OVERLAP on %s: previous ended at %s, next starts at %s (call_id=%d)%n",
//							number, prevEnd, c.getStartTime(), c.getCallId()
//					);
//					valid = false;
//				}
//				prevEnd = c.getEndTime();
//			}
//		}
//
//		if (valid) {
//			System.out.println("✅ All sessions are valid: no overlaps, no crossing-midnight records.");
//		} else {
//			System.err.println("❌ Validation failed, see errors above.");
//		}
//	}

