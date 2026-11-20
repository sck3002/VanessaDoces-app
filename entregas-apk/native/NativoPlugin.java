package br.com.vanessadoces.entregas;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* Funções nativas usadas pela tela de Entregas: listar os apps instalados e abrir um deles (o telefone Bluetooth do carro). */
@CapacitorPlugin(name = "Nativo")
public class NativoPlugin extends Plugin {

    private void adicionar(JSArray arr, String nome, String pacote) {
        JSObject o = new JSObject();
        o.put("nome", nome);
        o.put("pacote", pacote);
        arr.put(o);
    }

    @PluginMethod
    public void apps(PluginCall call) {
        try {
            PackageManager pm = getContext().getPackageManager();
            Set<String> vistos = new HashSet<String>();
            JSArray arr = new JSArray();

            /* 1) apps que aparecem no menu de aplicativos */
            Intent principal = new Intent(Intent.ACTION_MAIN, null);
            principal.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> lista = pm.queryIntentActivities(principal, 0);
            if (lista != null) {
                for (ResolveInfo ri : lista) {
                    String pacote = ri.activityInfo.packageName;
                    if (vistos.add(pacote)) {
                        adicionar(arr, String.valueOf(ri.loadLabel(pm)), pacote);
                    }
                }
            }

            /* 2) reserva: qualquer app instalado que tenha uma tela de abertura (alguns aparelhos escondem o app de telefone do menu) */
            List<ApplicationInfo> todos = pm.getInstalledApplications(0);
            if (todos != null) {
                for (ApplicationInfo ai : todos) {
                    if (!vistos.contains(ai.packageName) && pm.getLaunchIntentForPackage(ai.packageName) != null) {
                        vistos.add(ai.packageName);
                        adicionar(arr, String.valueOf(pm.getApplicationLabel(ai)), ai.packageName);
                    }
                }
            }

            JSObject ret = new JSObject();
            ret.put("apps", arr);
            call.resolve(ret);
        } catch (Exception e) {
            call.reject("erro ao listar apps: " + e);
        }
    }

    @PluginMethod
    public void abrirApp(PluginCall call) {
        String pacote = call.getString("pacote");
        if (pacote == null || pacote.isEmpty()) {
            call.reject("pacote ausente");
            return;
        }
        try {
            Intent it = getContext().getPackageManager().getLaunchIntentForPackage(pacote);
            if (it == null) {
                call.reject("app não encontrado: " + pacote);
                return;
            }
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(it);
            call.resolve();
        } catch (Exception e) {
            call.reject("erro ao abrir: " + e);
        }
    }
}
