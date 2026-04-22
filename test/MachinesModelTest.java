
import org.junit.jupiter.api.Test;
import ru.kurs.petrovkurs.model.Machines;

import static org.junit.jupiter.api.Assertions.*;

public class MachinesModelTest {

    @Test
    void testMachinesCreationAndProperties() {
        Machines machine = new Machines();
        machine.setMachinesId(1L);
        machine.setModel("Станок-1");
        machine.setInvNumber("INV-001");
        machine.setCommissionedAt(java.time.LocalDate.of(2023, 1, 15));

        assertEquals(1L, machine.getMachinesId());
        assertEquals("Станок-1", machine.getModel());
        assertEquals("INV-001", machine.getInvNumber());
        assertEquals(java.time.LocalDate.of(2023, 1, 15), machine.getCommissionedAt());
    }

    @Test
    void testMachinesPropertiesNotNull() {
        Machines machine = new Machines();
        machine.setModel("Тестовая модель");
        machine.setInvNumber("INV-TEST");
        machine.setCommissionedAt(java.time.LocalDate.now());

        assertNotNull(machine.getPropertyModel().get());
        assertNotNull(machine.getPropertyInvNumber().get());
        assertNotNull(machine.getPropertyCommissionedAt().get());
    }
}