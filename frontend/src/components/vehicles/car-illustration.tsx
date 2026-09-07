import { cn } from "@/lib/utils";

/** A decorative, model-neutral car: not a photo of the user's vehicle. */
export function CarIllustration({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 480 250" fill="none" aria-hidden="true" className={cn("w-full", className)}>
      <circle cx="294" cy="107" r="91" fill="#FFD337" />
      <path d="M16 208H455" stroke="#242424" strokeOpacity=".12" strokeWidth="2" strokeLinecap="round" />
      <path d="M26 233H88M111 233H141M321 233H420" stroke="#242424" strokeOpacity=".09" strokeWidth="3" strokeLinecap="round" />
      <path d="M50 88H112M74 76H107M400 143H440" stroke="#242424" strokeOpacity=".15" strokeWidth="3" strokeLinecap="round" />
      <ellipse cx="246" cy="208" rx="169" ry="9" fill="#242424" fillOpacity=".08" />
      <path d="M83 156 123 143 160 95C165 88 174 84 185 84H266C279 84 289 89 299 99L339 142 389 156C402 160 409 170 409 181V188C409 195 402 199 394 199H85C76 199 69 192 69 184V173C69 165 74 160 83 156Z" fill="#FCFCF9" stroke="#242424" strokeWidth="3" strokeLinejoin="round" />
      <path d="m139 141 32-41c2-3 6-5 10-5h39v46h-81Zm93-46h33c10 0 17 4 24 11l33 35h-90V95Z" fill="#D7DFD7" stroke="#242424" strokeWidth="2.5" strokeLinejoin="round" />
      <path d="M222 95v95M139 149v38M326 150l9 32" stroke="#242424" strokeOpacity=".2" strokeWidth="2" />
      <path d="M190 154h15m78 0h15M88 185h306" stroke="#242424" strokeWidth="3" strokeLinecap="round" />
      <path d="M80 166h22l-4 11H74m308-16h16l8 15h-24v-15Z" fill="#FFD337" stroke="#242424" strokeWidth="2" strokeLinejoin="round" />
      <circle cx="137" cy="190" r="28" fill="#242424" />
      <circle cx="137" cy="190" r="14" fill="#EEEFE8" />
      <circle cx="137" cy="190" r="5" fill="#9A9C92" />
      <circle cx="342" cy="190" r="28" fill="#242424" />
      <circle cx="342" cy="190" r="14" fill="#EEEFE8" />
      <circle cx="342" cy="190" r="5" fill="#9A9C92" />
      <path d="m372 46 3 10 10 3-10 3-3 10-3-10-10-3 10-3 3-10ZM112 101l2 7 7 2-7 2-2 7-2-7-7-2 7-2 2-7Z" fill="#242424" />
    </svg>
  );
}
