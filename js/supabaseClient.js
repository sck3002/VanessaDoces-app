// js/supabaseClient.js
import { createClient } from 'https://cdn.jsdelivr.net/npm/@supabase/supabase-js@2/+esm'

const supabaseUrl = 'https://stwqlmxymyixgbwxynxh.supabase.co/rest/v1/'
const supabaseKey = 'sb_publishable_jBjgD5yEzdHdGT1pEZ6Jkw_kj3lowPV' // Use a chave completa que você copiou

export const supabase = createClient(supabaseUrl, supabaseKey)
