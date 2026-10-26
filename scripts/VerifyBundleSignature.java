import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.jar.JarFile;

/** Verify every AAB payload through JarFile against the expected public certificate. */
public final class VerifyBundleSignature {
    public static void main(String[] args) throws Exception {
        if (args.length != 2 || !args[1].matches("[a-f0-9]{64}")) throw new IllegalArgumentException("AAB path and expected certificate SHA256 required");
        int payloads = 0;
        try (JarFile jar = new JarFile(Path.of(args[0]).toFile(), true)) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (entry.isDirectory()) continue;
                try (InputStream in = jar.getInputStream(entry)) { in.transferTo(OutputStream.nullOutputStream()); }
                if (entry.getName().startsWith("META-INF/")) continue;
                var signers = entry.getCodeSigners();
                if (signers == null || signers.length != 1) throw new SecurityException("Unsigned or unexpected signer count: " + entry.getName());
                var certificate = signers[0].getSignerCertPath().getCertificates().get(0);
                String actual = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(certificate.getEncoded()));
                if (!actual.equals(args[1])) throw new SecurityException("Unexpected certificate: " + entry.getName());
                payloads++;
            }
        }
        if (payloads == 0) throw new SecurityException("Empty bundle");
        System.out.println("PASS AAB JarFile integrity: " + payloads + " payload entries verified against pinned public certificate " + args[1]);
        System.out.println("Scope: signature/integrity, not certificate-chain trust, Play approval or installed behavior.");
    }
}
