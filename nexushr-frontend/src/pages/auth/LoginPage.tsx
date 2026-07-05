import { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useAuth } from '../../hooks/useAuth';
import { Button } from '../../components/ui/Button';

const schema = z.object({
  usernameOrEmail: z.string().min(1, 'Required'),
  password: z.string().min(1, 'Required'),
});
type FormData = z.infer<typeof schema>;

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const from = (location.state as { from?: { pathname: string } })?.from?.pathname ?? '/dashboard';

  const { register, handleSubmit, formState: { errors } } =
    useForm<FormData>({ resolver: zodResolver(schema) });

  const onSubmit = async (data: FormData) => {
    setError('');
    setLoading(true);
    try {
      await login(data.usernameOrEmail, data.password);
      navigate(from, { replace: true });
    } catch {
      setError('Invalid credentials. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen relative flex items-center justify-center font-sans overflow-hidden bg-slate-900">
      {/* Dynamic Animated Background */}
      <div className="absolute top-[-20%] left-[-10%] w-[70%] h-[70%] rounded-full bg-teal-600/20 blur-[120px] mix-blend-screen animate-pulse"></div>
      <div className="absolute bottom-[-20%] right-[-10%] w-[60%] h-[60%] rounded-full bg-emerald-600/20 blur-[120px] mix-blend-screen animate-pulse" style={{ animationDelay: '2s' }}></div>
      <div className="absolute top-[20%] right-[20%] w-[40%] h-[40%] rounded-full bg-cyan-600/20 blur-[100px] mix-blend-screen animate-pulse" style={{ animationDelay: '4s' }}></div>

      <div className="relative z-10 w-full max-w-md p-6 sm:p-10 mx-4 bg-white/10 backdrop-blur-2xl border border-white/20 rounded-[2rem] shadow-2xl">
        <div className="text-center mb-10">
          <div className="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-tr from-teal-500 to-emerald-400 rounded-2xl mb-6 shadow-lg shadow-teal-500/30">
            <span className="text-white text-3xl font-extrabold tracking-tight">N</span>
          </div>
          <h1 className="text-3xl font-bold text-white tracking-tight">Welcome Back</h1>
          <p className="text-slate-300 mt-2 font-medium">Sign in to continue to NexusHR</p>
        </div>

        {error && (
          <div className="mb-6 p-4 bg-red-500/10 border border-red-500/20 text-red-400 rounded-xl text-sm font-medium flex items-center gap-3 backdrop-blur-md">
            <svg className="w-5 h-5 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-slate-300 mb-2">Username or Email</label>
            <input
              type="text"
              autoComplete="username"
              className={`w-full px-5 py-3.5 bg-white/5 border ${errors.usernameOrEmail ? 'border-red-400/50 focus:ring-red-400' : 'border-white/10 focus:border-teal-400 focus:ring-teal-400'} rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-opacity-20 transition-all backdrop-blur-sm`}
              {...register('usernameOrEmail')}
            />
            {errors.usernameOrEmail && <p className="mt-1.5 text-xs text-red-400 font-medium">{errors.usernameOrEmail.message}</p>}
          </div>
          
          <div>
            <label className="block text-sm font-medium text-slate-300 mb-2">Password</label>
            <input
              type="password"
              autoComplete="current-password"
              className={`w-full px-5 py-3.5 bg-white/5 border ${errors.password ? 'border-red-400/50 focus:ring-red-400' : 'border-white/10 focus:border-teal-400 focus:ring-teal-400'} rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-opacity-20 transition-all backdrop-blur-sm`}
              {...register('password')}
            />
            {errors.password && <p className="mt-1.5 text-xs text-red-400 font-medium">{errors.password.message}</p>}
          </div>

          <Button type="submit" loading={loading} className="w-full mt-8 py-3.5 text-base font-semibold rounded-xl bg-gradient-to-r from-teal-600 to-emerald-500 hover:from-teal-500 hover:to-emerald-400 text-white shadow-lg shadow-teal-500/25 border border-white/10 transition-all">
            {loading ? 'Authenticating...' : 'Sign In'}
          </Button>
        </form>

        <p className="text-center text-sm text-slate-400 mt-8 font-medium">
          Don't have an account?{' '}
          <Link to="/register" className="text-teal-400 font-bold hover:text-teal-300 transition-colors">
            Register now
          </Link>
        </p>
      </div>
    </div>
  );
}