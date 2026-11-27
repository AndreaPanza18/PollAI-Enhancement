package com.example.pollai.benchmark.produzione;

import com.example.pollai.pollaio.Pollaio;
import com.example.pollai.produzione.DatiProduzione;
import com.example.pollai.produzione.ProduzioneDAO;
import com.example.pollai.produzione.ProduzioneService;
import org.hibernate.annotations.DynamicInsert;
import org.openjdk.jmh.annotations.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Measurement(iterations = 10, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@DynamicHalt(model = "oscnn")
public class ProduzioneBenchmark {

    private ProduzioneService produzioneService;
    private ProduzioneDAO produzioneDAO;

    private Pollaio pollaio;
    private DatiProduzione produzione;

    private LocalDate dataBenchmark;

    private List<Integer> uova;

    @Param({"10", "100", "1000", "10000"})
    private int numeroElementi;

    @Setup(Level.Trial)
    public void setupTrial() {
        produzioneDAO = mock(ProduzioneDAO.class);
        produzioneService = new ProduzioneService();
        ReflectionTestUtils.setField(produzioneService, "produzioneDAO", produzioneDAO);
        dataBenchmark = LocalDate.of(2026, 1, 1);
        uova = new ArrayList<>();

        for (int i = 0; i < numeroElementi; i++) {
            uova.add(10);
        }

        when(produzioneDAO.save(any(DatiProduzione.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(produzioneDAO.findUovaAfterDate(
                nullable(Long.class),
                any(LocalDate.class)
        )).thenReturn(uova);
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
        pollaio = new Pollaio();
        produzione = new DatiProduzione();
    }

    @Benchmark
    public DatiProduzione benchmarkCreateProduzione() {
        return produzioneService.createProduzione(pollaio);
    }

    @Benchmark
    public DatiProduzione benchmarkAggiornaUovaGiorno() {
        return produzioneService.aggiornaUovaGiorno(10, produzione, dataBenchmark);
    }

    @Benchmark
    public int benchmarkGetProduzionePeriodo() {
        return produzioneService.getProduzionePeriodo(dataBenchmark, produzione);
    }
}
