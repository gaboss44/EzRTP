package com.skyblockexp.ezrtp.command.subcommands;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.teleport.ChunkyProvider;
import com.skyblockexp.ezrtp.teleport.ChunkyWarmupCoordinator;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PregenerateSubcommandTest {

    @Mock
    EzRtpPlugin plugin;

    @Mock
    FileConfiguration config;

    @Mock
    CommandSender sender;

    @Test
    public void execute_whenChunkyMissing_sendsUnavailableMessage() {
        PregenerateSubcommand cmd = new PregenerateSubcommand(plugin, (ChunkyProvider) null, null);
        boolean result = cmd.execute(sender, new String[0]);

        assertTrue(result);
        verify(sender, atLeastOnce()).sendMessage(anyString());
    }
}
